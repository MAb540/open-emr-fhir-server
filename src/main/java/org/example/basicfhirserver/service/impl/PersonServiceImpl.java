package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.person.PersonSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserService;
import org.example.basicfhirserver.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {

  private final UserService userService;

  public PersonServiceImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDBRecord findById(UUID uuid) {
    List<UserDBRecord> userDBRecords = userService.findById(uuid);
    if (userDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Person with given ID " + uuid + " not found.");
    }
    return userDBRecords.get(0);
  }

  @Override
  public Page<UserDBRecord> find(PersonSearchQuery personSearchQuery) {
    return userService.find(personSearchQuery);
  }
}
