package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.Practitioner;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;

import java.util.List;
import java.util.UUID;

public interface PractitionerService {

    Practitioner findById(UUID uuid);

    List<Practitioner> find(PractitionerSearchQuery practitionerSearchQuery);
}
