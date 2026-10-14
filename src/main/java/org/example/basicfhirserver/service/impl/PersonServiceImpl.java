package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.person.PersonSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserRepository;
import org.example.basicfhirserver.service.PersonService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {

  private final UserRepository userRepository;

  public PersonServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDBRecord findById(UUID uuid) {
    List<UserDBRecord> userDBRecords = userRepository.findById(uuid);
    if (userDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Person with given ID " + uuid + " not found.");
    }
    return userDBRecords.get(0);
  }

  @Override
  public Page<UserDBRecord> find(PersonSearchQuery personSearchQuery) {
    return userRepository.find(personSearchQuery);
  }
}
