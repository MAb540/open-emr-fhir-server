package org.example.basicfhirserver.provider;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.ObservationSearchTranslator;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_BODY_WEIGHT,
    supported = {
      ProfilesConstants.HL7_US_CORE_BODY_WEIGHT,
      ProfilesConstants.HL7_US_CORE_BODY_HEIGHT,
      ProfilesConstants.HL7_US_CORE_BODY_TEMPERATURE,
      ProfilesConstants.HL7_US_CORE_BMI,
      ProfilesConstants.HL7_US_CORE_BLOOD_PRESSURE
    })
public class ObservationResourceProvider implements IResourceProvider {

  private final ObservationService observationService;
  private final ObservationSearchTranslator observationSearchTranslator;

  public ObservationResourceProvider(
      @Qualifier("ObservationServiceImpl") ObservationService observationService,
      ObservationSearchTranslator observationSearchTranslator) {
    this.observationService = observationService;
    this.observationSearchTranslator = observationSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Observation.class;
  }

  @Read()
  public Observation getResourceById(@IdParam IdType theId) {
    return observationService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchObservations(
      @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = Observation.SP_CATEGORY) TokenOrListParam category,
      @OptionalParam(name = Observation.SP_CODE) TokenOrListParam codes,
      @OptionalParam(name = Observation.SP_DATE) DateParam date,
      @OptionalParam(name = Observation.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @IncludeParam(allow = {"Observation:subject", "Observation:patient", "Observation:encounter"})
          Set<Include> theIncludes,
      @Count Integer count,
      @Offset Integer offset) {
    ObservationSearchCriteria criteria =
        ObservationSearchCriteria.builder()
            .patient(patient)
            .category(category)
            .codes(codes)
            .date(date)
            .lastUpdated(lastUpdated)
            .includes(theIncludes)
            .count(count)
            .offset(offset)
            .build();

    var observationSearchQuery = observationSearchTranslator.translate(criteria);
    SearchResult searchResult = observationService.search(observationSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
