package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.Practitioner;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PractitionerService {

    Practitioner findById(UUID uuid);

    Page<Practitioner> find(PractitionerSearchQuery practitionerSearchQuery);
}
