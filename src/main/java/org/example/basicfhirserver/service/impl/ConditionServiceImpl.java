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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConditionServiceImpl implements ConditionService {

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
    public List<ConditionCanonical> find(ConditionSearchQuery conditionSearchQuery) {

        List<ConditionCanonical> canonicalConditions = new ArrayList<>();


        if (conditionSearchQuery.getCategory() != null && conditionSearchQuery.getCategory().equals(ConditionAssembler.CATEGORY_PROBLEM_LIST)) {
            List<ConditionProblemListItemDBRecord> problemLists = conditionRepository.findConditionProblemListItem(conditionSearchQuery);
            problemLists.forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));

        } else if (conditionSearchQuery.getCategory() != null && conditionSearchQuery.getCategory().equals(ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS)) {
            List<ConditionEncounterDiagnosisDBRecord> encounterDiag = conditionRepository.findConditionEncounterDiagnosis(conditionSearchQuery);
            encounterDiag.forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));

        } else if (conditionSearchQuery.getCategory() != null && conditionSearchQuery.getCategory().equals(ConditionAssembler.CATEGORY_HEALTH_CONCERNS)) {
            List<ConditionHealthConcernDBRecord> healthConcerns = conditionRepository.findConditionHealthConcernDiagnosis(conditionSearchQuery);
            healthConcerns.forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
        } else {
            List<ConditionProblemListItemDBRecord> problemLists = conditionRepository.findConditionProblemListItem(conditionSearchQuery);
            List<ConditionEncounterDiagnosisDBRecord> encounterDiag = conditionRepository.findConditionEncounterDiagnosis(conditionSearchQuery);
            List<ConditionHealthConcernDBRecord> healthConcerns = conditionRepository.findConditionHealthConcernDiagnosis(conditionSearchQuery);

            problemLists.forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
            encounterDiag.forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
            healthConcerns.forEach(row -> canonicalConditions.add(conditionAssembler.toCanonical(row)));
        }

        return canonicalConditions;
    }
}
