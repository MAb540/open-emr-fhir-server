package org.example.basicfhirserver.provider;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.AllergyIntoleranceMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.impl.AllergyIntoleranceSearchTranslator;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class AllergyIntoleranceProvider implements IResourceProvider {


    private final AllergyIntoleranceService allergyIntoleranceService;
    private final AllergyIntoleranceMapper allergyIntoleranceMapper;
    private final AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator;
    private final PatientService patientService;
    private final LegacyPatientMapper legacyPatientMapper;

    public AllergyIntoleranceProvider(AllergyIntoleranceService allergyIntoleranceService,
                                      AllergyIntoleranceMapper allergyIntoleranceMapper,
                                      AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator,
                                      PatientService patientService,
                                      LegacyPatientMapper legacyPatientMapper
    ) {
        this.allergyIntoleranceService = allergyIntoleranceService;
        this.allergyIntoleranceMapper = allergyIntoleranceMapper;
        this.allergyIntoleranceSearchTranslator = allergyIntoleranceSearchTranslator;
        this.patientService = patientService;
        this.legacyPatientMapper = legacyPatientMapper;
    }


    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return AllergyIntolerance.class;
    }

    @Read()
    public AllergyIntolerance getResourceById(@IdParam IdType theId) {
        AllergyDBRecord allergyDBRecord = allergyIntoleranceService.findById(UUID.fromString(theId.getIdPart()));
        return allergyIntoleranceMapper.toR4(allergyDBRecord);
    }

    @Search()
    public IBundleProvider searchAllergyIntolerance(
            @OptionalParam(name = AllergyIntolerance.SP_RES_ID) TokenParam id,
            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
            @IncludeParam(allow = {
                    "AllergyIntolerance:patient"
            })
            Set<Include> theIncludes,
            @Count Integer count,
            @Offset Integer offset
    ) {
        AllergyIntoleranceSearchCriteria criteria = AllergyIntoleranceSearchCriteria.builder()
                .id(id)
                .patient(patient)
                .count(count)
                .offset(offset)
                .build();

        var allergyIntoleranceSearchQuery = allergyIntoleranceSearchTranslator.translate(criteria);
        List<AllergyDBRecord> allergyDBRecords = allergyIntoleranceService.find(allergyIntoleranceSearchQuery);

        List<IBaseResource> primaryAllergyIntolerances = allergyDBRecords.stream()
                .<IBaseResource>map(allergyIntoleranceMapper::toR4)
                .toList();

        boolean includePatients = theIncludes != null && theIncludes.stream()
                .anyMatch(inc -> "AllergyIntolerance:patient".equals(inc.getValue()));

        List<IBaseResource> includedResources = new ArrayList<>();

        if (!allergyDBRecords.isEmpty()) {

            if (includePatients) {
                List<String> patientUuids = allergyDBRecords.stream()
                        .map(record -> record.getPatientUuid().toString())
                        .toList();

                PatientSearchQuery query = PatientSearchQuery.builder()
                        .patientId(patientUuids)
                        .build();

                Page<LegacyPatientEntity> legacyPatientEntities = patientService.find(query);
                legacyPatientEntities.getContent()
                        .stream()
                        .<IBaseResource>map(legacyPatientMapper::toR4)
                        .forEach(includedResources::add);
            }
        }

        int currentOffset = offset != null ? offset : 0;
        int currentPageSize = primaryAllergyIntolerances.size();

        return new BundleProvider(
                primaryAllergyIntolerances,
                includedResources,
                currentPageSize,
                currentOffset,
                currentPageSize
        );
    }

}
