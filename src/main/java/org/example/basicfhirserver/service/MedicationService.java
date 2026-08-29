package org.example.basicfhirserver.service;

import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;

import java.util.List;
import java.util.UUID;

public interface MedicationService {

    DrugDBRecord findById(UUID uuid);

    List<DrugDBRecord> find();

}
