package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.MedicationRequestSearchTranslator;
import org.example.basicfhirserver.service.MedicationRequestService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_MEDICATIONREQUEST,
    supported = {ProfilesConstants.HL7_US_CORE_MEDICATIONREQUEST})
public class MedicationRequestResourceProvider implements IResourceProvider {

  private final MedicationRequestService medicationRequestService;
  private final MedicationRequestSearchTranslator medicationRequestSearchTranslator;

  public MedicationRequestResourceProvider(
      MedicationRequestService medicationRequestService,
      MedicationRequestSearchTranslator medicationRequestSearchTranslator) {
    this.medicationRequestService = medicationRequestService;
    this.medicationRequestSearchTranslator = medicationRequestSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return MedicationRequest.class;
  }

  @Read()
  public MedicationRequest getResourceById(@IdParam IdType theId) {
    return medicationRequestService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchMedicationRequest(
      @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = MedicationRequest.SP_INTENT) TokenOrListParam intent,
      @OptionalParam(name = MedicationRequest.SP_STATUS) StringParam status,
      @Count Integer count,
      @Offset Integer offset) {
    MedicationRequestSearchCriteria criteria =
        MedicationRequestSearchCriteria.builder()
            .patient(patient)
            .intent(intent)
            .status(status)
            .count(count)
            .offset(offset)
            .build();

    var medicationRequestSearchQuery = medicationRequestSearchTranslator.translate(criteria);
    SearchResult searchResult = medicationRequestService.search(medicationRequestSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
