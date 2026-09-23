package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionEncounterDiagnosisDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionHealthConcernDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionProblemListItemDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionRepository;
import org.example.basicfhirserver.service.ConditionService;
import org.example.basicfhirserver.service.assembler.condition.ConditionAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConditionServiceImpl implements ConditionService {

    private static final int DEFAULT_PAGE_SIZE = 5;

    private final ConditionRepository conditionRepository;
    private final ConditionAssembler conditionAssembler;

    public ConditionServiceImpl(ConditionRepository conditionRepository, ConditionAssembler conditionAssembler) {
        this.conditionRepository = conditionRepository;
        this.conditionAssembler = conditionAssembler;
    }

    @Override
    public ConditionCanonical findById(UUID uuid) {

        List<ConditionProblemListItemDBRecord> problemLists = conditionRepository.findConditionProblemListItemById(uuid);
        List<ConditionEncounterDiagnosisDBRecord> encounterDiag = conditionRepository.findConditionEncounterDiagnosisById(uuid);
        List<ConditionHealthConcernDBRecord> healthConcerns = conditionRepository.findConditionHealthConcernDiagnosisById(uuid);

        if (problemLists.isEmpty() && encounterDiag.isEmpty() && healthConcerns.isEmpty()) {
            throw new ResourceNotFoundException("Condition with given ID " + uuid + " not found.");
        }

        if (!problemLists.isEmpty()) {
            return conditionAssembler.toCanonical(problemLists.get(0));
        }
        if (!encounterDiag.isEmpty()) {
            return conditionAssembler.toCanonical(encounterDiag.get(0));
        }

        return conditionAssembler.toCanonical(healthConcerns.get(0));

    }

    @Override
    public Page<ConditionCanonical> find(ConditionSearchQuery conditionSearchQuery) {

        int limit = conditionSearchQuery.getCount() != null ? conditionSearchQuery.getCount() : DEFAULT_PAGE_SIZE;
        int offset = conditionSearchQuery.getOffset() != null ? conditionSearchQuery.getOffset() : 0;
        Pageable pageable = PageRequest.of(offset / limit, limit);

        String category = conditionSearchQuery.getCategory();
        ConditionSearchQuery pagedQuery = withPaging(conditionSearchQuery, limit, offset);

        if (category != null && category.equals(ConditionAssembler.CATEGORY_PROBLEM_LIST)) {
            Page<ConditionProblemListItemDBRecord> page = conditionRepository.findConditionProblemListItem(pagedQuery);
            return toCanonicalPage(page.getContent().stream().map(conditionAssembler::toCanonical).toList(), pageable, page.getTotalElements());
        }

        if (category != null && category.equals(ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS)) {
            Page<ConditionEncounterDiagnosisDBRecord> page = conditionRepository.findConditionEncounterDiagnosis(pagedQuery);
            return toCanonicalPage(page.getContent().stream().map(conditionAssembler::toCanonical).toList(), pageable, page.getTotalElements());
        }

        if (category != null && category.equals(ConditionAssembler.CATEGORY_HEALTH_CONCERNS)) {
            Page<ConditionHealthConcernDBRecord> page = conditionRepository.findConditionHealthConcernDiagnosis(pagedQuery);
            return toCanonicalPage(page.getContent().stream().map(conditionAssembler::toCanonical).toList(), pageable, page.getTotalElements());
        }

        ConditionSearchQuery unpagedQuery = withPaging(conditionSearchQuery, null, offset);
        List<ConditionCanonical> canonicalConditions = new ArrayList<>();

        conditionRepository.findConditionProblemListItem(unpagedQuery).getContent()
                .forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
        conditionRepository.findConditionEncounterDiagnosis(unpagedQuery).getContent()
                .forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
        conditionRepository.findConditionHealthConcernDiagnosis(unpagedQuery).getContent()
                .forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));

        long total = canonicalConditions.size();
        int fromIndex = Math.min(offset, canonicalConditions.size());
        int toIndex = Math.min(fromIndex + limit, canonicalConditions.size());

        return toCanonicalPage(new ArrayList<>(canonicalConditions.subList(fromIndex, toIndex)), pageable, total);
    }

    private Page<ConditionCanonical> toCanonicalPage(List<ConditionCanonical> content, Pageable pageable, long total) {
        return new PageImpl<>(content, pageable, total);
    }

    private ConditionSearchQuery withPaging(ConditionSearchQuery query, Integer count, Integer offset) {
        return ConditionSearchQuery.builder()
                .patientId(query.getPatientId())
                .category(query.getCategory())
                .count(count)
                .offset(offset)
                .build();
    }
}
