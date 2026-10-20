package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.OrganizationSearchTranslator;
import org.example.basicfhirserver.service.OrganizationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_ORGANIZATION,
    supported = {ProfilesConstants.HL7_US_CORE_ORGANIZATION})
public class OrganizationProvider implements IResourceProvider {

  private final OrganizationService organizationService;
  private final OrganizationSearchTranslator organizationSearchTranslator;

  public OrganizationProvider(
      OrganizationService organizationService,
      OrganizationSearchTranslator organizationSearchTranslator) {
    this.organizationService = organizationService;
    this.organizationSearchTranslator = organizationSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Organization.class;
  }

  @Read()
  public Organization getResourceById(@IdParam IdType theId) {
    return organizationService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchOrganizations(
      @OptionalParam(name = Organization.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Organization.SP_NAME) StringParam name,
      @OptionalParam(name = Organization.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @Count Integer count,
      @Offset Integer offset) {
    OrganizationSearchCriteria criteria =
        OrganizationSearchCriteria.builder()
            .id(id)
            .name(name)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .build();

    var organizationSearchQuery = organizationSearchTranslator.translate(criteria);
    SearchResult searchResult = organizationService.search(organizationSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
