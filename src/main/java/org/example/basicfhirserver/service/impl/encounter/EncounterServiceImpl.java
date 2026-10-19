package org.example.basicfhirserver.service.impl.encounter;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterDBRecord;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterRepository;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.assembler.formencounter.FormEncounterAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class EncounterServiceImpl implements EncounterService {

  private final FormEncounterAssembler formEncounterAssembler;
  private final FormEncounterRepository formEncounterRepository;
  private final EncounterMapper encounterMapper;

  public EncounterServiceImpl(
      FormEncounterRepository formEncounterRepository,
      FormEncounterAssembler formEncounterAssembler,
      EncounterMapper encounterMapper) {
    this.formEncounterRepository = formEncounterRepository;
    this.formEncounterAssembler = formEncounterAssembler;
    this.encounterMapper = encounterMapper;
  }

  @Override
  public Encounter findById(UUID uuid) {
    List<FormEncounterDBRecord> formEncountersDBRecords = formEncounterRepository.findById(uuid);
    if (formEncountersDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Encounter with given ID " + uuid + " not found.");
    }

    return encounterMapper.toR4(formEncounterAssembler.toCanonical(formEncountersDBRecords.get(0)));
  }

  @Override
  public SearchResult search(EncounterSearchQuery encounterSearchQuery) {
    Page<FormEncounterDBRecord> formEncountersDBRecords =
        formEncounterRepository.find(encounterSearchQuery);

    List<IBaseResource> primaryEncounters =
        formEncountersDBRecords.getContent().stream()
            .map(formEncounterAssembler::toCanonical)
            .<IBaseResource>map(encounterMapper::toR4)
            .toList();

    int currentOffset =
        encounterSearchQuery.getOffset() != null ? encounterSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryEncounters,
        List.of(),
        (int) formEncountersDBRecords.getTotalElements(),
        currentOffset);
  }
}
