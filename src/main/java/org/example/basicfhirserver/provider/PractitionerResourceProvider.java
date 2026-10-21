package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.PractitionerSearchTranslator;
import org.example.basicfhirserver.service.PractitionerService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_PRACTITIONER,
    supported = {ProfilesConstants.HL7_US_CORE_PRACTITIONER})
public class PractitionerResourceProvider implements IResourceProvider {

  private final PractitionerService practitionerService;
  private final PractitionerSearchTranslator practitionerSearchTranslator;

  public PractitionerResourceProvider(
      PractitionerService practitionerService,
      PractitionerSearchTranslator practitionerSearchTranslator) {
    this.practitionerService = practitionerService;
    this.practitionerSearchTranslator = practitionerSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Practitioner.class;
  }

  @Read()
  public Practitioner getResourceById(@IdParam IdType theId) {
    return practitionerService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchPractitioners(
      @OptionalParam(name = Practitioner.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Practitioner.SP_NAME) StringParam name,
      @OptionalParam(name = Practitioner.SP_IDENTIFIER) TokenParam identifier,
      @Count Integer count,
      @Offset Integer offset) {
    PractitionerSearchCriteria criteria =
        PractitionerSearchCriteria.builder()
            .id(id)
            .name(name)
            .identifier(identifier)
            .count(count)
            .offset(offset)
            .build();

    var practitionerSearchQuery = practitionerSearchTranslator.translate(criteria);
    SearchResult searchResult = practitionerService.search(practitionerSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
