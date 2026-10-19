package org.example.basicfhirserver.service.impl.medicationRequest;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.MedicationRequestMapper;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.repository.jdbc.prescription.FacilityDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionRepository;
import org.example.basicfhirserver.service.MedicationRequestService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class MedicationRequestServiceImpl implements MedicationRequestService {

  private final PrescriptionRepository prescriptionRepository;
  private final MedicationRequestMapper medicationRequestMapper;

  public MedicationRequestServiceImpl(
      PrescriptionRepository prescriptionRepository,
      MedicationRequestMapper medicationRequestMapper) {
    this.prescriptionRepository = prescriptionRepository;
    this.medicationRequestMapper = medicationRequestMapper;
  }

  @Override
  public MedicationRequest findById(UUID uuid) {
    List<PrescriptionDBRecord> prescriptionDBRecords = prescriptionRepository.findById(uuid);
    if (prescriptionDBRecords.isEmpty()) {
      throw new ResourceNotFoundException(
          "Medication Request with given ID " + uuid + " not found.");
    }
    return medicationRequestMapper.toR4(prescriptionDBRecords.get(0));
  }

  @Override
  public SearchResult search(MedicationRequestSearchQuery medicationRequestSearchQuery) {
    Page<PrescriptionDBRecord> prescriptionDBRecords =
        prescriptionRepository.find(medicationRequestSearchQuery);

    List<FacilityDBRecord> facilityDBRecords = prescriptionRepository.findFacility();
    if (!facilityDBRecords.isEmpty()) {
      FacilityDBRecord facilityDBRecord = facilityDBRecords.get(0);
      prescriptionDBRecords
          .getContent()
          .forEach(prescription -> prescription.setOrganizationUuid(facilityDBRecord.getUuid()));
    }

    List<IBaseResource> primaryMedicationRequests =
        prescriptionDBRecords.getContent().stream()
            .<IBaseResource>map(medicationRequestMapper::toR4)
            .toList();

    int currentOffset =
        medicationRequestSearchQuery.getOffset() != null
            ? medicationRequestSearchQuery.getOffset()
            : 0;

    return new SearchResult(
        primaryMedicationRequests,
        List.of(),
        (int) prescriptionDBRecords.getTotalElements(),
        currentOffset);
  }
}
