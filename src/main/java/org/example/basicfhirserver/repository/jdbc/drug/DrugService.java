package org.example.basicfhirserver.repository.jdbc.drug;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.springframework.data.domain.Page;

public interface DrugService {

  List<DrugDBRecord> findById(UUID uuid);

  Page<DrugDBRecord> find(MedicationSearchQuery medicationSearchQuery);
}
