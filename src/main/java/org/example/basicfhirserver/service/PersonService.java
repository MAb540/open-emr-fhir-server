package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.person.PersonSearchQuery;
import org.example.basicfhirserver.service.impl.person.PersonSearchResult;
import org.hl7.fhir.r4.model.Person;

public interface PersonService {

  Person findById(UUID uuid);

  PersonSearchResult find(PersonSearchQuery personSearchQuery);
}
