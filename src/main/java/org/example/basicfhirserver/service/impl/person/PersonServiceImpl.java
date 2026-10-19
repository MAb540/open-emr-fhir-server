package org.example.basicfhirserver.service.impl.person;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.PersonMapper;
import org.example.basicfhirserver.query.resources.person.PersonSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserRepository;
import org.example.basicfhirserver.service.PersonService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {

  private final UserRepository userRepository;
  private final PersonMapper personMapper;

  public PersonServiceImpl(UserRepository userRepository, PersonMapper personMapper) {
    this.userRepository = userRepository;
    this.personMapper = personMapper;
  }

  @Override
  public Person findById(UUID uuid) {
    List<UserDBRecord> userDBRecords = userRepository.findById(uuid);
    if (userDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Person with given ID " + uuid + " not found.");
    }
    return personMapper.toR4(userDBRecords.get(0));
  }

  @Override
  public SearchResult search(PersonSearchQuery personSearchQuery) {
    Page<UserDBRecord> personDBRecords = userRepository.find(personSearchQuery);

    List<IBaseResource> primaryPersons =
        personDBRecords.getContent().stream().<IBaseResource>map(personMapper::toR4).toList();

    int currentOffset = personSearchQuery.getOffset() != null ? personSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryPersons, List.of(), (int) personDBRecords.getTotalElements(), currentOffset);
  }
}
