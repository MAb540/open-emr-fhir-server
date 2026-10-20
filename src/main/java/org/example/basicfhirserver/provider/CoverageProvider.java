package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.coverage.CoverageSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.CoverageSearchTranslator;
import org.example.basicfhirserver.service.CoverageService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_COVERAGE,
    supported = {ProfilesConstants.HL7_US_CORE_COVERAGE})
public class CoverageProvider implements IResourceProvider {

  private final CoverageService coverageService;
  private final CoverageSearchTranslator coverageSearchTranslator;

  public CoverageProvider(
      CoverageService coverageService, CoverageSearchTranslator coverageSearchTranslator) {
    this.coverageService = coverageService;
    this.coverageSearchTranslator = coverageSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Coverage.class;
  }

  @Read()
  public Coverage getResourceById(@IdParam IdType theId) {
    return coverageService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchCoverage(
      @OptionalParam(name = Coverage.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Coverage.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = "status") TokenParam status,
      @Count Integer count,
      @Offset Integer offset) {
    CoverageSearchCriteria criteria =
        CoverageSearchCriteria.builder()
            .id(id)
            .patient(patient)
            .status(status)
            .count(count)
            .offset(offset)
            .build();

    var coverageSearchQuery = coverageSearchTranslator.translate(criteria);
    SearchResult searchResult = coverageService.search(coverageSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
