package org.example.basicfhirserver.service.impl.condition;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionEncounterDiagnosisDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionHealthConcernDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionProblemListItemDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionRepository;
import org.example.basicfhirserver.service.ConditionService;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.assembler.condition.ConditionAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ConditionServiceImpl implements ConditionService {

  private static final int DEFAULT_PAGE_SIZE = 5;

  private final ConditionRepository conditionRepository;
  private final ConditionAssembler conditionAssembler;
  private final ConditionMapper conditionMapper;
  private final PatientService patientService;
  private final EncounterService encounterService;

  public ConditionServiceImpl(
      ConditionRepository conditionRepository,
      ConditionAssembler conditionAssembler,
      ConditionMapper conditionMapper,
      PatientService patientService,
      EncounterService encounterService) {
    this.conditionRepository = conditionRepository;
    this.conditionAssembler = conditionAssembler;
    this.conditionMapper = conditionMapper;
    this.patientService = patientService;
    this.encounterService = encounterService;
  }

  @Override
  public Condition findById(UUID uuid) {
    List<ConditionProblemListItemDBRecord> problemLists =
        conditionRepository.findConditionProblemListItemById(uuid);
    List<ConditionEncounterDiagnosisDBRecord> encounterDiag =
        conditionRepository.findConditionEncounterDiagnosisById(uuid);
    List<ConditionHealthConcernDBRecord> healthConcerns =
        conditionRepository.findConditionHealthConcernDiagnosisById(uuid);

    if (problemLists.isEmpty() && encounterDiag.isEmpty() && healthConcerns.isEmpty()) {
      throw new ResourceNotFoundException("Condition with given ID " + uuid + " not found.");
    }

    if (!problemLists.isEmpty()) {
      return conditionMapper.toR4(conditionAssembler.toCanonical(problemLists.get(0)));
    }
    if (!encounterDiag.isEmpty()) {
      return conditionMapper.toR4(conditionAssembler.toCanonical(encounterDiag.get(0)));
    }

    return conditionMapper.toR4(conditionAssembler.toCanonical(healthConcerns.get(0)));
  }

  @Override
  public SearchResult search(ConditionSearchQuery conditionSearchQuery) {
    Page<ConditionCanonical> conditionsCanonical = findCanonical(conditionSearchQuery);

    List<IBaseResource> primaryConditions =
        conditionsCanonical.getContent().stream()
            .<IBaseResource>map(conditionMapper::toR4)
            .toList();

    List<IBaseResource> includedResources =
        includeResources(conditionsCanonical, conditionSearchQuery.getIncludes());

    int currentOffset =
        conditionSearchQuery.getOffset() != null ? conditionSearchQuery.getOffset() : 0;
    return new SearchResult(
        primaryConditions,
        includedResources,
        (int) conditionsCanonical.getTotalElements(),
        currentOffset);
  }

  private Page<ConditionCanonical> findCanonical(ConditionSearchQuery conditionSearchQuery) {
    int limit =
        conditionSearchQuery.getCount() != null
            ? conditionSearchQuery.getCount()
            : DEFAULT_PAGE_SIZE;
    int offset = conditionSearchQuery.getOffset() != null ? conditionSearchQuery.getOffset() : 0;
    Pageable pageable = PageRequest.of(offset / limit, limit);

    String category = conditionSearchQuery.getCategory();
    ConditionSearchQuery pagedQuery = withPaging(conditionSearchQuery, limit, offset);

    if (category != null && category.equals(ConditionAssembler.CATEGORY_PROBLEM_LIST)) {
      Page<ConditionProblemListItemDBRecord> page =
          conditionRepository.findConditionProblemListItem(pagedQuery);
      return toCanonicalPage(
          page.getContent().stream().map(conditionAssembler::toCanonical).toList(),
          pageable,
          page.getTotalElements());
    }

    if (category != null && category.equals(ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS)) {
      Page<ConditionEncounterDiagnosisDBRecord> page =
          conditionRepository.findConditionEncounterDiagnosis(pagedQuery);
      return toCanonicalPage(
          page.getContent().stream().map(conditionAssembler::toCanonical).toList(),
          pageable,
          page.getTotalElements());
    }

    if (category != null && category.equals(ConditionAssembler.CATEGORY_HEALTH_CONCERNS)) {
      Page<ConditionHealthConcernDBRecord> page =
          conditionRepository.findConditionHealthConcernDiagnosis(pagedQuery);
      return toCanonicalPage(
          page.getContent().stream().map(conditionAssembler::toCanonical).toList(),
          pageable,
          page.getTotalElements());
    }

    ConditionSearchQuery unpagedQuery = withPaging(conditionSearchQuery, null, offset);
    List<ConditionCanonical> canonicalConditions = new ArrayList<>();

    conditionRepository
        .findConditionProblemListItem(unpagedQuery)
        .getContent()
        .forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
    conditionRepository
        .findConditionEncounterDiagnosis(unpagedQuery)
        .getContent()
        .forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
    conditionRepository
        .findConditionHealthConcernDiagnosis(unpagedQuery)
        .getContent()
        .forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));

    long total = canonicalConditions.size();
    int fromIndex = Math.min(offset, canonicalConditions.size());
    int toIndex = Math.min(fromIndex + limit, canonicalConditions.size());

    return toCanonicalPage(
        new ArrayList<>(canonicalConditions.subList(fromIndex, toIndex)), pageable, total);
  }

  private List<IBaseResource> includeResources(
      Page<ConditionCanonical> conditionsCanonical, Set<Include> includes) {
    boolean includeSubjects =
        includes != null
            && includes.stream().anyMatch(inc -> "Condition:subject".equals(inc.getValue()));

    boolean includeEncounters =
        includes != null
            && includes.stream().anyMatch(inc -> "Condition:encounter".equals(inc.getValue()));

    List<IBaseResource> includedResources = new ArrayList<>();

    if (conditionsCanonical.isEmpty()) {
      return includedResources;
    }

    if (includeSubjects) {
      List<String> patientUuids =
          conditionsCanonical.getContent().stream()
              .map(ConditionCanonical::getPatientUuid)
              .filter(uuid -> uuid != null && !uuid.isEmpty())
              .distinct()
              .toList();

      if (!patientUuids.isEmpty()) {
        PatientSearchQuery query = PatientSearchQuery.builder().patientId(patientUuids).build();

        SearchResult patientSearchResult = patientService.search(query);
        includedResources.addAll(patientSearchResult.primaryResources());
      }
    }

    if (includeEncounters) {
      List<String> encounterUuids =
          conditionsCanonical.getContent().stream()
              .map(ConditionCanonical::getEncounterUuid)
              .filter(uuid -> uuid != null && !uuid.isEmpty())
              .distinct()
              .toList();

      if (!encounterUuids.isEmpty()) {
        EncounterSearchQuery query =
            EncounterSearchQuery.builder().encounterId(encounterUuids).build();
        SearchResult encounterSearchResult = encounterService.search(query);
        includedResources.addAll(encounterSearchResult.primaryResources());
      }
    }

    return includedResources;
  }

  private Page<ConditionCanonical> toCanonicalPage(
      List<ConditionCanonical> content, Pageable pageable, long total) {
    return new PageImpl<>(content, pageable, total);
  }

  private ConditionSearchQuery withPaging(
      ConditionSearchQuery query, Integer count, Integer offset) {
    return ConditionSearchQuery.builder()
        .patientId(query.getPatientId())
        .category(query.getCategory())
        .lastUpdated(query.getLastUpdated())
        .count(count)
        .offset(offset)
        .build();
  }
}
