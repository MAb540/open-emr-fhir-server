package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.example.basicfhirserver.repository.jdbc.drug.DrugService;
import org.example.basicfhirserver.service.MedicationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final DrugService drugService;

    public MedicationServiceImpl(DrugService drugService) {
        this.drugService = drugService;
    }

    @Override
    public DrugDBRecord findById(UUID uuid) {

        List<DrugDBRecord> drugDBRecords = drugService.findById(uuid);
        if (drugDBRecords.isEmpty()) {
            throw new ResourceNotFoundException("Medication with given ID " + uuid + " not found.");
        }

        return drugDBRecords.get(0);
    }

    @Override
    public List<DrugDBRecord> find() {

        return drugService.find();
    }
}
