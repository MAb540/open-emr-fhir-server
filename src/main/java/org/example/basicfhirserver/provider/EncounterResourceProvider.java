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
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.EncounterSearchTranslator;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_ENCOUNTER,
    supported = {ProfilesConstants.HL7_US_CORE_ENCOUNTER})
public class EncounterResourceProvider implements IResourceProvider {

  private final EncounterService encounterService;
  private final EncounterSearchTranslator encounterSearchTranslator;

  public EncounterResourceProvider(
      EncounterService encounterService, EncounterSearchTranslator encounterSearchTranslator) {
    this.encounterService = encounterService;
    this.encounterSearchTranslator = encounterSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Encounter.class;
  }

  @Read()
  public Encounter getResourceById(@IdParam IdType theId) {
    return encounterService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchEncounters(
      @OptionalParam(name = Encounter.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Encounter.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = Encounter.SP_DATE) DateParam date,
      @OptionalParam(name = Encounter.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @Count Integer count,
      @Offset Integer offset) {
    EncounterSearchCriteria criteria =
        EncounterSearchCriteria.builder()
            .id(id)
            .patient(patient)
            .date(date)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .build();

    var encounterSearchQuery = encounterSearchTranslator.translate(criteria);
    SearchResult searchResult = encounterService.search(encounterSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
