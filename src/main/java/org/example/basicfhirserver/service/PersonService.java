package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.person.PersonSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.springframework.data.domain.Page;

public interface PersonService {

  UserDBRecord findById(UUID uuid);

  Page<UserDBRecord> find(PersonSearchQuery personSearchQuery);
}
