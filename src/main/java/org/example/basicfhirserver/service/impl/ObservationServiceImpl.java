package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsUuidMappingDBRecord;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.assembler.vitals.ObservationUuidUtil;
import org.example.basicfhirserver.service.assembler.vitals.VitalObservationAssembler;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsDBRecord;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("ObservationServiceImpl")
public class ObservationServiceImpl implements ObservationService {

    private final VitalsService vitalService;
    private final VitalObservationAssembler vitalObservationAssembler;

    public ObservationServiceImpl(VitalObservationAssembler vitalObservationAssembler,
                                  VitalsService vitalService) {
        this.vitalObservationAssembler = vitalObservationAssembler;
        this.vitalService = vitalService;
    }


    @Override
    public VitalObservation findById(UUID uuid) {
        List<VitalsUuidMappingDBRecord> mappings = vitalService.findVitalsUuidMappingsById(uuid);
        VitalsUuidMappingDBRecord mapping = mappings.get(0);
        List<VitalsDBRecord> vitalRows = vitalService.findVitalsById(mapping.getTargetUuid());

        if (mappings.isEmpty() || vitalRows.isEmpty()) {
            throw new ResourceNotFoundException("Observation with given ID " + uuid + " not found.");
        }

        VitalsDBRecord vitalRow = vitalRows.get(0);
        List<VitalObservation> observations =
                vitalObservationAssembler.toCanonical(
                        vitalRow,
                        List.of(mapping)
                );

        return observations.get(0);
    }

    @Override
    public List<VitalObservation> find(ObservationSearchQuery observationSearchQuery) {

        List<VitalsDBRecord> vitalRows = vitalService.findVitals(observationSearchQuery);

        if (vitalRows.isEmpty()) {
            return List.of();
        }

        List<UUID> vitalsUuids = vitalRows.stream().map(VitalsDBRecord::getVitalsUuid).toList();

        List<VitalsUuidMappingDBRecord> mappings = vitalService.findVitalsUuidMappings(vitalsUuids);

        Map<UUID, List<VitalsUuidMappingDBRecord>> mappingsByVitalsUuid = mappings.stream().collect(Collectors.groupingBy(VitalsUuidMappingDBRecord::getTargetUuid));

        boolean hasCodeFilter = observationSearchQuery.getCodes() != null && !observationSearchQuery.getCodes().isEmpty();

        Set<String> requestedCodes = hasCodeFilter ? observationSearchQuery.getCodes().stream().map(SearchValue::getValue).collect(Collectors.toSet()) : Set.of();

        return vitalRows.stream().flatMap(row -> {
            List<VitalsUuidMappingDBRecord> rowMappings = mappingsByVitalsUuid.getOrDefault(row.getVitalsUuid(), List.of());

            if (hasCodeFilter) {
                rowMappings = rowMappings.stream().filter(mapping ->
                        requestedCodes.contains(ObservationUuidUtil.getCode(mapping.getResourcePath()))).toList();
            }

            return vitalObservationAssembler.toCanonical(row, rowMappings).stream();
        }).toList();
    }
}




