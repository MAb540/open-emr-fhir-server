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
import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.ConditionTranslator;
import org.example.basicfhirserver.service.ConditionService;
import org.example.basicfhirserver.service.impl.condition.ConditionSearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_CONDITION,
    supported = {
      ProfilesConstants.HL7_US_CORE_CONDITION_ENCOUNTER_DIAGNOSIS,
      ProfilesConstants.HL7_US_CORE_CONDITION_PROBLEMS_HEALTH_CONCERNS
    })
public class ConditionResourceProvider implements IResourceProvider {

  private final ConditionService conditionService;
  private final ConditionTranslator conditionTranslator;

  public ConditionResourceProvider(
      ConditionService conditionService, ConditionTranslator conditionTranslator) {
    this.conditionService = conditionService;
    this.conditionTranslator = conditionTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Condition.class;
  }

  @Read()
  public Condition getResourceById(@IdParam IdType theId) {
    return conditionService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchConditions(
      @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = Observation.SP_CATEGORY) TokenParam category,
      @OptionalParam(name = Condition.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @IncludeParam(allow = {"Condition:subject", "Condition:encounter"}) Set<Include> includes,
      @Count Integer count,
      @Offset Integer offset) {
    ConditionSearchCriteria criteria =
        ConditionSearchCriteria.builder()
            .patient(patient)
            .category(category)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .includes(includes)
            .build();

    var conditionSearchQuery = conditionTranslator.translate(criteria);
    ConditionSearchResult searchResult = conditionService.find(conditionSearchQuery);

    int currentOffset = offset != null ? offset : 0;

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        Math.toIntExact(searchResult.totalElements()),
        currentOffset,
        searchResult.primaryResources().size());
  }
}
