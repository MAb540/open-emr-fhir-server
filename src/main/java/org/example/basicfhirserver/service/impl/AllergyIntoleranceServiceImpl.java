package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyService;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AllergyIntoleranceServiceImpl implements AllergyIntoleranceService {

    private final AllergyService allergyService;

    public AllergyIntoleranceServiceImpl(AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @Override
    public AllergyDBRecord findById(UUID uuid) {
        List<AllergyDBRecord> allergyDBRecords = allergyService.findById(uuid);
        if(allergyDBRecords.isEmpty()){
            throw new ResourceNotFoundException("AllergyIntolerance with given ID " + uuid + " not found.");
        }
        return allergyDBRecords.get(0);
    }

    @Override
    public List<AllergyDBRecord> find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery) {

        return allergyService.find(allergyIntoleranceSearchQuery);
    }
}
