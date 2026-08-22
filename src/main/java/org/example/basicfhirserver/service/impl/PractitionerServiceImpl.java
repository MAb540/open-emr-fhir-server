package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.model.Practitioner;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserService;
import org.example.basicfhirserver.service.PractitionerService;
import org.example.basicfhirserver.service.assembler.users.UserAssembler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PractitionerServiceImpl implements PractitionerService {

    private final UserService userService;
    private final UserAssembler userAssembler;

    private PractitionerServiceImpl(
            UserService userService,
            UserAssembler userAssembler
    ) {
        this.userService = userService;
        this.userAssembler = userAssembler;
    }

    @Override
    public Practitioner findById(UUID uuid) {
        List<UserDBRecord> userDBRecords = userService.findById(uuid);
        if(userDBRecords.isEmpty()){
            throw new ResourceNotFoundException("Practitioner with given ID " + uuid + " not found.");
        }

        return userAssembler.toCanonical(userDBRecords.get(0));
    }

    @Override
    public List<Practitioner> find(PractitionerSearchQuery practitionerSearchQuery) {
        List<UserDBRecord> userDBRecords = userService.find(practitionerSearchQuery);
        return userDBRecords.stream().map(userAssembler::toCanonical).toList();
    }
}
