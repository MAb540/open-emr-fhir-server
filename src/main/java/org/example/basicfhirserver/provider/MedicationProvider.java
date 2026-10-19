package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.MedicationSearchTranslator;
import org.example.basicfhirserver.service.MedicationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_MEDICATION,
    supported = {ProfilesConstants.HL7_MEDICATION})
public class MedicationProvider implements IResourceProvider {

  private final MedicationService medicationService;
  private final MedicationSearchTranslator medicationSearchTranslator;

  public MedicationProvider(
      MedicationService medicationService, MedicationSearchTranslator medicationSearchTranslator) {
    this.medicationService = medicationService;
    this.medicationSearchTranslator = medicationSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Medication.class;
  }

  @Read()
  public Medication getResourceById(@IdParam IdType theId) {
    return medicationService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchMedication(@Count Integer count, @Offset Integer offset) {
    MedicationSearchCriteria medicationSearchCriteria =
        MedicationSearchCriteria.builder().count(count).offset(offset).build();

    var medicationSearchQuery = medicationSearchTranslator.translate(medicationSearchCriteria);
    SearchResult searchResult = medicationService.search(medicationSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
