package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.repository.jdbc.prescription.FacilityDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionRepository;
import org.example.basicfhirserver.service.MedicationRequestService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class MedicationRequestImpl implements MedicationRequestService {

  private final PrescriptionRepository prescriptionRepository;

  public MedicationRequestImpl(PrescriptionRepository prescriptionRepository) {
    this.prescriptionRepository = prescriptionRepository;
  }

  @Override
  public PrescriptionDBRecord findById(UUID uuid) {
    List<PrescriptionDBRecord> prescriptionDBRecords = prescriptionRepository.findById(uuid);
    if (prescriptionDBRecords.isEmpty()) {
      throw new ResourceNotFoundException(
          "Medication Request with given ID " + uuid + " not found.");
    }
    return prescriptionDBRecords.get(0);
  }

  @Override
  public Page<PrescriptionDBRecord> find(
      MedicationRequestSearchQuery medicationRequestSearchQuery) {
    Page<PrescriptionDBRecord> prescriptionDBRecords =
        prescriptionRepository.find(medicationRequestSearchQuery);

    List<FacilityDBRecord> facilityDBRecords = prescriptionRepository.findFacility();
    if (!facilityDBRecords.isEmpty()) {
      FacilityDBRecord facilityDBRecord = facilityDBRecords.get(0);
      prescriptionDBRecords
          .getContent()
          .forEach(prescription -> prescription.setOrganizationUuid(facilityDBRecord.getUuid()));
    }

    return prescriptionDBRecords;
  }
}
