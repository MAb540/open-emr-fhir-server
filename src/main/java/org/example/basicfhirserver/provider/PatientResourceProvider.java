package org.example.basicfhirserver.provider;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.provider.validator.FhirResponseValidationService;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.PatientSearchTranslator;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class PatientResourceProvider implements IResourceProvider {

    private final LegacyPatientMapper legacyPatientMapper;
    private final PatientSearchTranslator patientSearchTranslator;
    private final FhirResponseValidationService validationService;
    private final PatientService patientService;
    private final ObservationService observationService;
    private final ObservationMapper observationMapper;
    private final EncounterService encounterService;
    private final EncounterMapper encounterMapper;

    public PatientResourceProvider(
            LegacyPatientMapper legacyPatientMapper,
            PatientSearchTranslator patientSearchTranslator,
            FhirResponseValidationService validationService,
            PatientService patientService,
            @Qualifier("ObservationServiceImpl") ObservationService observationService,
            ObservationMapper observationMapper,
            EncounterService encounterService,
            EncounterMapper encounterMapper
    ) {
        this.legacyPatientMapper = legacyPatientMapper;
        this.patientSearchTranslator = patientSearchTranslator;
        this.validationService = validationService;
        this.patientService = patientService;
        this.observationService = observationService;
        this.observationMapper = observationMapper;
        this.encounterService = encounterService;
        this.encounterMapper = encounterMapper;
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Read()
    public Patient getResourceById(@IdParam IdType theId) {
        LegacyPatientEntity legacyPatientEntity = patientService.findById(UUID.fromString(theId.getIdPart()));
        return legacyPatientMapper.toR4(legacyPatientEntity);
    }

    @Search()
    public IBundleProvider searchPatients(
            @OptionalParam(name = Patient.SP_RES_ID) TokenParam id,
            @OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Patient.SP_FAMILY) StringParam family,
            @OptionalParam(name = Patient.SP_GIVEN) StringParam given,
            @OptionalParam(name = Patient.SP_NAME) StringParam name,
            @OptionalParam(name = Patient.SP_BIRTHDATE) DateParam birthDate,
            @OptionalParam(name = Patient.SP_DEATH_DATE) DateParam deathDate,
            @IncludeParam(allow = {
                    "Patient:organization",
                    "Patient:general-practitioner"
            })
            Set<Include> theIncludes,
            @IncludeParam(reverse = true, allow = {
                    "Observation:patient",
                    "Observation:subject",
                    "Encounter:patient"
            })
            Set<Include> theRevIncludes,
            @Count Integer count,
            @Offset Integer offset
    ) {
        PatientSearchCriteria criteria = PatientSearchCriteria.builder()
                .id(id)
                .identifier(identifier)
                .family(family)
                .given(given)
                .name(name)
                .birthdate(birthDate)
                .deathDate(deathDate)
                .count(count)
                .offset(offset)
                .build();

        var patientSearchQuery = patientSearchTranslator.translate(criteria);
        Page<LegacyPatientEntity> legacyPatientEntities = patientService.find(patientSearchQuery);

        List<IBaseResource> primaryPatients = legacyPatientEntities.getContent()
                .stream()
                .<IBaseResource>map(legacyPatientMapper::toR4)
                .toList();

        boolean includeObservations = theRevIncludes != null && theRevIncludes.stream()
                .anyMatch(inc -> "Observation:patient".equals(inc.getValue()) || "Observation:subject".equals(inc.getValue()));

        boolean includeEncounters = theRevIncludes != null && theRevIncludes.stream()
                .anyMatch(inc -> "Encounter:patient".equals(inc.getValue()));

        List<IBaseResource> includedObservations = new ArrayList<>();

        if ( !legacyPatientEntities.isEmpty()) {
            List<String> patientUuids = legacyPatientEntities.getContent().stream()
                    .map(lp -> lp.getUuid().toString())
                    .toList();

            if(includeObservations){
                ObservationSearchQuery query = ObservationSearchQuery.builder()
                        .patientId(patientUuids)
                        .build();

                List<VitalObservation> observations = observationService.find(query);
                observations.stream()
                        .map(observationMapper::toR4)
                        .forEach(includedObservations::add);
            }

            if(includeEncounters){
                EncounterSearchQuery query = EncounterSearchQuery.builder()
                        .patientId(patientUuids)
                        .build();
                List<FormEncounter> formEncounters = encounterService.find(query);
                formEncounters.stream()
                        .map(encounterMapper::toR4)
                        .forEach(includedObservations::add);
            }
        }

        int currentOffset = offset != null ? offset : 0;
        int currentPageSize = legacyPatientEntities.getContent().size();

        return new BundleProvider(
                primaryPatients,
                includedObservations,
                Math.toIntExact(legacyPatientEntities.getTotalElements()),
                currentOffset,
                currentPageSize
        );

//        return legacyPatientEntities
//                .stream().map(legacyPatientEntity -> {
//                    Patient patient = legacyPatientMapper.toR4Patient(legacyPatientEntity);
////                    ValidationResult result =
////                            validationService.validate(patient);
////                    if (!result.isSuccessful()) {
////                        throw new RuntimeException("Resource is not valid according to profile");
////                    }
//                   return patient;
//                }).toList();
    }

    @Create
    public MethodOutcome createPatient(@ResourceParam Patient incomingPatient) {
        // Extract raw primitives from the incoming structural FHIR model
        String familyName = incomingPatient.hasName() ? incomingPatient.getNameFirstRep().getFamily() : "UNKNOWN";
        String givenName = incomingPatient.hasName() ? incomingPatient.getNameFirstRep().getGivenAsSingleString() : "UNKNOWN";

        List<CanonicalType> profiles = incomingPatient.getMeta().getProfile();

        for (CanonicalType p : profiles) {
            System.out.println("profile value " + p.getValueAsString());
        }

        boolean isUsCore = profiles.stream()
                .anyMatch(p -> p.getValueAsString().contains("us-core-patient"));

        LocalDate dob = null;
        if (incomingPatient.hasBirthDate()) {
            dob = incomingPatient.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        String genderString = incomingPatient.hasGender() ? incomingPatient.getGender().toCode() : "unknown";
//        String assignedLegacyId = legacyStorage.savePatient(givenName, familyName, dob, genderString);
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("Patient", "1"));
        return outcome;
    }

}
