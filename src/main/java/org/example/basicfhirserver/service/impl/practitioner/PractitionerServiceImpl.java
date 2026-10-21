package org.example.basicfhirserver.service.impl.practitioner;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.PractitionerMapper;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserRepository;
import org.example.basicfhirserver.service.PractitionerService;
import org.example.basicfhirserver.service.assembler.users.UserAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class PractitionerServiceImpl implements PractitionerService {

  private final UserRepository userRepository;
  private final UserAssembler userAssembler;
  private final PractitionerMapper practitionerMapper;

  public PractitionerServiceImpl(
      UserRepository userRepository,
      UserAssembler userAssembler,
      PractitionerMapper practitionerMapper) {
    this.userRepository = userRepository;
    this.userAssembler = userAssembler;
    this.practitionerMapper = practitionerMapper;
  }

  @Override
  public Practitioner findById(UUID uuid) {
    List<UserDBRecord> userDBRecords = userRepository.findById(uuid);
    if (userDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Practitioner with given ID " + uuid + " not found.");
    }

    return practitionerMapper.toR4(userAssembler.toCanonical(userDBRecords.get(0)));
  }

  @Override
  public SearchResult search(PractitionerSearchQuery practitionerSearchQuery) {
    Page<UserDBRecord> userDBRecords = userRepository.find(practitionerSearchQuery);

    List<IBaseResource> primaryPractitioners =
        userDBRecords.getContent().stream()
            .map(userAssembler::toCanonical)
            .map(practitionerMapper::toR4)
            .map(IBaseResource.class::cast)
            .toList();

    int currentOffset =
        practitionerSearchQuery.getOffset() != null ? practitionerSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryPractitioners, List.of(), (int) userDBRecords.getTotalElements(), currentOffset);
  }
}
