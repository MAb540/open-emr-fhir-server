package org.example.basicfhirserver.provider;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.impl.ConditionTranslator;
import org.example.basicfhirserver.service.ConditionService;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class ConditionResourceProvider implements IResourceProvider {

    private final ConditionService conditionService;
    private final ConditionMapper conditionMapper;
    private final ConditionTranslator conditionTranslator;
    private final PatientService patientService;
    private final LegacyPatientMapper legacyPatientMapper;
    private final EncounterService encounterService;
    private final EncounterMapper encounterMapper;

    public ConditionResourceProvider(ConditionService conditionService,
                                     ConditionMapper conditionMapper,
                                     ConditionTranslator conditionTranslator,
                                     PatientService patientService,
                                     LegacyPatientMapper legacyPatientMapper,
                                     EncounterService encounterService,
                                     EncounterMapper encounterMapper) {
        this.conditionService = conditionService;
        this.conditionMapper = conditionMapper;
        this.conditionTranslator = conditionTranslator;
        this.patientService = patientService;
        this.legacyPatientMapper = legacyPatientMapper;
        this.encounterService = encounterService;
        this.encounterMapper = encounterMapper;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Condition.class;
    }

    @Read()
    public Condition getResourceById(@IdParam IdType theId) {
        ConditionCanonical conditionsCanonical = conditionService.findById(UUID.fromString(theId.getIdPart()));
        return conditionMapper.toR4(conditionsCanonical);
    }

    @Search()
    public IBundleProvider searchConditions(
            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = Observation.SP_CATEGORY) TokenParam category,
            @IncludeParam(allow = {
                    "Condition:subject",
                    "Condition:encounter"
            })
            Set<Include> theIncludes,
            @Count Integer count,
            @Offset Integer offset
    ) {
        ConditionSearchCriteria criteria = ConditionSearchCriteria.builder()
                .patient(patient)
                .category(category)
                .count(count)
                .offset(offset)
                .build();

        var conditionSearchQuery = conditionTranslator.translate(criteria);
        Page<ConditionCanonical> conditionsCanonical = conditionService.find(conditionSearchQuery);

        List<IBaseResource> primaryConditions = conditionsCanonical.getContent().stream()
                .<IBaseResource>map(conditionMapper::toR4)
                .toList();

        boolean includeSubjects = theIncludes != null && theIncludes.stream()
                .anyMatch(inc -> "Condition:subject".equals(inc.getValue()));

        boolean includeEncounters = theIncludes != null && theIncludes.stream()
                .anyMatch(inc -> "Condition:encounter".equals(inc.getValue()));

        List<IBaseResource> includedResources = new ArrayList<>();

        if (!conditionsCanonical.isEmpty()) {
            if (includeSubjects) {
                List<String> patientUuids = conditionsCanonical.getContent().stream()
                        .map(ConditionCanonical::getPatientUuid)
                        .filter(uuid -> uuid != null && !uuid.isEmpty())
                        .distinct()
                        .toList();

                if (!patientUuids.isEmpty()) {
                    PatientSearchQuery query = PatientSearchQuery.builder()
                            .patientId(patientUuids)
                            .build();

                    Page<LegacyPatientEntity> legacyPatientEntities = patientService.find(query);
                    legacyPatientEntities.getContent()
                            .stream()
                            .<IBaseResource>map(legacyPatientMapper::toR4)
                            .forEach(includedResources::add);
                }
            }
            if(includeEncounters){
                List<String> encounterUuids = conditionsCanonical.getContent().stream()
                        .map(ConditionCanonical::getEncounterUuid)
                        .filter(uuid -> uuid != null && !uuid.isEmpty())
                        .distinct()
                        .toList();

                EncounterSearchQuery query = EncounterSearchQuery.builder()
                        .encounterId(encounterUuids)
                        .build();

                List<FormEncounter> formEncounters = encounterService.find(query).getContent();
                formEncounters.stream()
                        .map(encounterMapper::toR4)
                        .forEach(includedResources::add);
            }
        }

        int currentOffset = offset != null ? offset : 0;
        int currentPageSize = conditionsCanonical.getContent().size();

        return new BundleProvider(
                primaryConditions,
                includedResources,
                Math.toIntExact(conditionsCanonical.getTotalElements()),
                currentOffset,
                currentPageSize
        );
    }
}
