package org.example.basicfhirserver.service.impl.immunization;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ImmunizationMapper;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.immunization.ImmunizationDBRecord;
import org.example.basicfhirserver.repository.jdbc.immunization.ImmunizationRepository;
import org.example.basicfhirserver.service.ImmunizationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Immunization;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationServiceImpl implements ImmunizationService {

  private final ImmunizationRepository immunizationRepository;
  private final ImmunizationMapper immunizationMapper;

  public ImmunizationServiceImpl(
      ImmunizationRepository immunizationRepository, ImmunizationMapper immunizationMapper) {
    this.immunizationRepository = immunizationRepository;
    this.immunizationMapper = immunizationMapper;
  }

  @Override
  public Immunization findById(UUID uuid) {
    List<ImmunizationDBRecord> immunizationDBRecords = immunizationRepository.findById(uuid);
    if (immunizationDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Immunization with given ID " + uuid + " not found.");
    }
    return immunizationMapper.toR4(immunizationDBRecords.get(0));
  }

  @Override
  public SearchResult search(ImmunizationSearchQuery immunizationSearchQuery) {
    Page<ImmunizationDBRecord> immunizationDBRecords =
        immunizationRepository.find(immunizationSearchQuery);

    List<IBaseResource> primaryImmunizations =
        immunizationDBRecords.getContent().stream()
            .<IBaseResource>map(immunizationMapper::toR4)
            .toList();

    int currentOffset =
        immunizationSearchQuery.getOffset() != null ? immunizationSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryImmunizations,
        List.of(),
        (int) immunizationDBRecords.getTotalElements(),
        currentOffset);
  }
}
