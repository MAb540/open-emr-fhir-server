package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.provider.validator.FhirResponseValidationService;
import org.example.basicfhirserver.query.resources.patient.PatientSearchCriteria;
import org.example.basicfhirserver.service.*;
import org.example.basicfhirserver.query.translator.impl.PatientSearchTranslator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Component
public class PatientResourceProvider implements IResourceProvider {

    private final LegacyPatientMapper legacyPatientMapper;
    private final PatientSearchTranslator patientSearchTranslator;
    private final FhirResponseValidationService validationService;
    private final PatientService patientService;

    public PatientResourceProvider(
                       LegacyPatientMapper legacyPatientMapper,
                       PatientSearchTranslator patientSearchTranslator,
                       FhirResponseValidationService validationService,
                       PatientService patientService
                       ) {
        this.legacyPatientMapper = legacyPatientMapper;
        this.patientSearchTranslator = patientSearchTranslator;
        this.validationService = validationService;
        this.patientService = patientService;
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
            @OptionalParam(name = Patient.SP_NAME)  StringParam name,
            @OptionalParam(name = Patient.SP_BIRTHDATE) DateParam birthDate,
            @OptionalParam(name = Patient.SP_DEATH_DATE) DateParam deathDate,
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

        List<IBaseResource> resources =
                legacyPatientEntities.getContent()
                        .stream()
                        .<IBaseResource>map(legacyPatientMapper::toR4)
                        .toList();

        return new SimpleBundleProvider(resources).setSize(Math.toIntExact(
                legacyPatientEntities.getTotalElements()
        ));

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

        for( CanonicalType p : profiles ){
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
