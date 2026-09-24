package org.example.basicfhirserver.repository.jdbc.drug;

import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface DrugService {

    List<DrugDBRecord> findById(UUID uuid);

    Page<DrugDBRecord> find(MedicationSearchQuery medicationSearchQuery);

}
