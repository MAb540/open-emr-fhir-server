package org.example.basicfhirserver.repository.jdbc.prescription;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.springframework.data.domain.Page;

public interface PrescriptionService {

  List<PrescriptionDBRecord> findById(UUID uuid);

  Page<PrescriptionDBRecord> find(MedicationRequestSearchQuery medicationRequestSearchQuery);

  List<FacilityDBRecord> findFacility();
}
