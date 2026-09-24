package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.PractitionerMapper;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.PractitionerSearchTranslator;
import org.example.basicfhirserver.service.PractitionerService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PractitionerResourceProvider implements IResourceProvider {

    private final PractitionerService practitionerService;
    private final PractitionerMapper practitionerMapper;
    private final PractitionerSearchTranslator practitionerSearchTranslator;

    public PractitionerResourceProvider(
            PractitionerService practitionerService,
            PractitionerMapper practitionerMapper,
            PractitionerSearchTranslator practitionerSearchTranslator
    ) {
        this.practitionerService = practitionerService;
        this.practitionerMapper = practitionerMapper;
        this.practitionerSearchTranslator = practitionerSearchTranslator;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Practitioner.class;
    }


    @Read()
    public Practitioner getResourceById(@IdParam IdType theId) {
        org.example.basicfhirserver.model.Practitioner formEncounter = practitionerService.findById(UUID.fromString(theId.getIdPart()));
        return practitionerMapper.toR4(formEncounter);
    }

    @Search()
    public IBundleProvider searchPractitioners(
            @OptionalParam(name = Practitioner.SP_RES_ID) TokenParam id,
            @OptionalParam(name = Practitioner.SP_NAME) StringParam name,
            @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
            @Count Integer count,
            @Offset Integer offset
    ) {
        PractitionerSearchCriteria criteria = PractitionerSearchCriteria.builder()
                .id(id)
                .name(name)
                .identifier(identifier)
                .count(count)
                .offset(offset)
                .build();

        var practitionerSearchQuery = practitionerSearchTranslator.translate(criteria);
        Page<org.example.basicfhirserver.model.Practitioner> practitioners = practitionerService.find(practitionerSearchQuery);

        List<IBaseResource> primaryPractitioners = practitioners.getContent().stream()
                .<IBaseResource>map(practitionerMapper::toR4)
                .toList();

        int currentOffset = offset != null ? offset : 0;
        int currentPageSize = practitioners.getContent().size();

        return new BundleProvider(
                primaryPractitioners,
                List.of(),
                Math.toIntExact(practitioners.getTotalElements()),
                currentOffset,
                currentPageSize
        );
    }


}
