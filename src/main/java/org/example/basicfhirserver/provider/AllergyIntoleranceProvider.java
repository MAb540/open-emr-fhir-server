package org.example.basicfhirserver.provider;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.AllergyIntoleranceSearchTranslator;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.example.basicfhirserver.service.impl.allergyIntolerance.AllergySearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_ALLERGYINTOLERANCE,
    supported = {ProfilesConstants.HL7_US_CORE_ALLERGYINTOLERANCE})
public class AllergyIntoleranceProvider implements IResourceProvider {
  private final AllergyIntoleranceService allergyIntoleranceService;
  private final AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator;

  public AllergyIntoleranceProvider(
      AllergyIntoleranceService allergyIntoleranceService,
      AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator) {
    this.allergyIntoleranceService = allergyIntoleranceService;
    this.allergyIntoleranceSearchTranslator = allergyIntoleranceSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return AllergyIntolerance.class;
  }

  @Read()
  public AllergyIntolerance getResourceById(@IdParam IdType theId) {
    return allergyIntoleranceService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchAllergyIntolerance(
      @OptionalParam(name = AllergyIntolerance.SP_RES_ID) TokenParam id,
      @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = AllergyIntolerance.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @IncludeParam(allow = {"AllergyIntolerance:patient"}) Set<Include> includes,
      @Count Integer count,
      @Offset Integer offset) {
    AllergyIntoleranceSearchCriteria criteria =
        AllergyIntoleranceSearchCriteria.builder()
            .id(id)
            .patient(patient)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .includes(includes)
            .build();

    var allergyIntoleranceSearchQuery = allergyIntoleranceSearchTranslator.translate(criteria);
    AllergySearchResult searchResult =
        allergyIntoleranceService.find(allergyIntoleranceSearchQuery);

    int currentOffset = offset != null ? offset : 0;

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        Math.toIntExact(searchResult.totalElements()),
        currentOffset,
        searchResult.primaryResources().size());
  }
}
