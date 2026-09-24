package org.example.basicfhirserver.service;

import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MedicationService {

    DrugDBRecord findById(UUID uuid);

    Page<DrugDBRecord> find(MedicationSearchQuery medicationSearchQuery);

}
