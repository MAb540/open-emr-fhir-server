package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.model.Practitioner;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserRepository;
import org.example.basicfhirserver.service.PractitionerService;
import org.example.basicfhirserver.service.assembler.users.UserAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

@Service
public class PractitionerServiceImpl implements PractitionerService {

  private final UserRepository userRepository;
  private final UserAssembler userAssembler;

  private PractitionerServiceImpl(UserRepository userRepository, UserAssembler userAssembler) {
    this.userRepository = userRepository;
    this.userAssembler = userAssembler;
  }

  @Override
  public Practitioner findById(UUID uuid) {
    List<UserDBRecord> userDBRecords = userRepository.findById(uuid);
    if (userDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Practitioner with given ID " + uuid + " not found.");
    }

    return userAssembler.toCanonical(userDBRecords.get(0));
  }

  @Override
  public Page<Practitioner> find(PractitionerSearchQuery practitionerSearchQuery) {
    Page<UserDBRecord> userDBRecords = userRepository.find(practitionerSearchQuery);
    List<Practitioner> practitioners =
        userDBRecords.getContent().stream().map(userAssembler::toCanonical).toList();

    return new PageImpl<>(
        practitioners, userDBRecords.getPageable(), userDBRecords.getTotalElements());
  }
}
