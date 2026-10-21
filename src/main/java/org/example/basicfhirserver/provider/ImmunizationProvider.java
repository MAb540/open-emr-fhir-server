package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.ImmunizationSearchTranslator;
import org.example.basicfhirserver.service.ImmunizationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Immunization;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_IMMUNIZATION,
    supported = {ProfilesConstants.HL7_US_CORE_IMMUNIZATION})
public class ImmunizationProvider implements IResourceProvider {

  private final ImmunizationService immunizationService;
  private final ImmunizationSearchTranslator immunizationSearchTranslator;

  public ImmunizationProvider(
      ImmunizationService immunizationService,
      ImmunizationSearchTranslator immunizationSearchTranslator) {
    this.immunizationService = immunizationService;
    this.immunizationSearchTranslator = immunizationSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Immunization.class;
  }

  @Read()
  public Immunization getResourceById(@IdParam IdType theId) {
    return immunizationService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchImmunization(
      @OptionalParam(name = Immunization.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Immunization.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = Immunization.SP_DATE) DateParam date,
      @OptionalParam(name = Immunization.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @Count Integer count,
      @Offset Integer offset) {
    ImmunizationSearchCriteria criteria =
        ImmunizationSearchCriteria.builder()
            .id(id)
            .patient(patient)
            .date(date)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .build();

    var immunizationSearchQuery = immunizationSearchTranslator.translate(criteria);
    SearchResult searchResult = immunizationService.search(immunizationSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
