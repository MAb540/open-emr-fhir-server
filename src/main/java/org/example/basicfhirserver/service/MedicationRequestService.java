package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.springframework.data.domain.Page;

public interface MedicationRequestService {

  PrescriptionDBRecord findById(UUID uuid);

  Page<PrescriptionDBRecord> find(MedicationRequestSearchQuery medicationRequestSearchQuery);
}
