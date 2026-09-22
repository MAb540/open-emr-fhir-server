package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.AllergyIntoleranceMapper;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.AllergyIntoleranceSearchTranslator;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AllergyIntoleranceProvider implements IResourceProvider {


    private final AllergyIntoleranceService allergyIntoleranceService;
    private final AllergyIntoleranceMapper allergyIntoleranceMapper;
    private final AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator;

    public AllergyIntoleranceProvider(AllergyIntoleranceService allergyIntoleranceService,
                                      AllergyIntoleranceMapper allergyIntoleranceMapper,
                                      AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator
    ) {
        this.allergyIntoleranceService = allergyIntoleranceService;
        this.allergyIntoleranceMapper = allergyIntoleranceMapper;
        this.allergyIntoleranceSearchTranslator = allergyIntoleranceSearchTranslator;
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
    public List<AllergyIntolerance> searchAllergyIntolerance(
            @OptionalParam(name = AllergyIntolerance.SP_RES_ID) TokenParam id,
            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient
    ) {
        AllergyIntoleranceSearchCriteria criteria = AllergyIntoleranceSearchCriteria.builder()
                .id(id)
                .patient(patient)
                .build();

        var allergyIntoleranceSearchQuery = allergyIntoleranceSearchTranslator.translate(criteria);
        List<AllergyDBRecord> allergyDBRecords = allergyIntoleranceService.find(allergyIntoleranceSearchQuery);

        return allergyDBRecords.stream()
                .map(allergyIntoleranceMapper::toR4)
                .toList();
    }

}
