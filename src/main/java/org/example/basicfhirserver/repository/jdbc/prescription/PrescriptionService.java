package org.example.basicfhirserver.repository.jdbc.prescription;

import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;

import java.util.List;
import java.util.UUID;

public interface PrescriptionService {

    List<PrescriptionDBRecord> findById(UUID uuid);

    List<PrescriptionDBRecord> find(MedicationRequestSearchQuery medicationRequestSearchQuery);

    List<FacilityDBRecord> findFacility();

}
