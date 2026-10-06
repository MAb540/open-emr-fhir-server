package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.model.Practitioner;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.springframework.data.domain.Page;

public interface PractitionerService {

  Practitioner findById(UUID uuid);

  Page<Practitioner> find(PractitionerSearchQuery practitionerSearchQuery);
}
