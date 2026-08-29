package org.example.basicfhirserver.service;

import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;

import java.util.List;
import java.util.UUID;

public interface MedicationRequestService {

    PrescriptionDBRecord findById(UUID uuid);

    List<PrescriptionDBRecord> find(MedicationRequestSearchQuery medicationRequestSearchQuery);

}
