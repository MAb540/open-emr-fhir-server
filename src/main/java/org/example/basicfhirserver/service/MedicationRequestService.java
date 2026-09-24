package org.example.basicfhirserver.service;

import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MedicationRequestService {

    PrescriptionDBRecord findById(UUID uuid);

    Page<PrescriptionDBRecord> find(MedicationRequestSearchQuery medicationRequestSearchQuery);

}
