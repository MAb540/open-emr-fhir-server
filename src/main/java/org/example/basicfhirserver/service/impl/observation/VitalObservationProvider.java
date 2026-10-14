package org.example.basicfhirserver.service.impl.observation;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsDBRecord;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsRepository;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsUuidMappingDBRecord;
import org.example.basicfhirserver.service.assembler.vitals.ObservationUuidUtil;
import org.example.basicfhirserver.service.assembler.vitals.VitalObservationProviderAssembler;
import org.springframework.stereotype.Component;

@Component
public class VitalObservationProvider implements ObservationProvider {

    private final VitalsRepository vitalService;
    private final VitalObservationProviderAssembler vitalObservationAssembler;

    private static final String CATEGORY_VITAL_SIGNS = "vital-signs";

    public VitalObservationProvider(VitalsRepository vitalService, VitalObservationProviderAssembler vitalObservationAssembler) {
        this.vitalService = vitalService;
        this.vitalObservationAssembler = vitalObservationAssembler;
    }

    public Optional<CanonicalObservation> fetchById(UUID uuid) {
        List<VitalsUuidMappingDBRecord> mappings = vitalService.findVitalsUuidMappingsById(uuid);
        VitalsUuidMappingDBRecord mapping = mappings.get(0);
        List<VitalsDBRecord> vitalRows = vitalService.findVitalsById(mapping.getTargetUuid());

        if (mappings.isEmpty() || vitalRows.isEmpty()) {
            throw new ResourceNotFoundException("Observation with given ID " + uuid + " not found.");
        }

        VitalsDBRecord vitalRow = vitalRows.get(0);
        List<CanonicalObservation> observations =
                vitalObservationAssembler.toCanonical(vitalRow, List.of(mapping));

        return observations.isEmpty() ? Optional.empty() : Optional.of(observations.get(0));
    }


    @Override
    public boolean supports(ObservationSearchQuery searchQuery) {
        if (searchQuery.getCategory() != null && !searchQuery.getCategory().isEmpty()) {
            return searchQuery.getCategory().stream()
                    .anyMatch(cat -> CATEGORY_VITAL_SIGNS.equalsIgnoreCase(cat.getValue()));
        }

        if (searchQuery.getCodes() != null && !searchQuery.getCodes().isEmpty()) {
            return searchQuery.getCodes().stream()
                    .anyMatch(code ->
                            Boolean.parseBoolean(ObservationUuidUtil.getCode(code.getValue()))
                    );
        }
        return searchQuery.getCategory() == null && searchQuery.getCodes() == null;
    }

    @Override
    public List<CanonicalObservation> fetch(ObservationSearchQuery observationSearchQuery) {
        List<VitalsDBRecord> vitalRows = vitalService.findVitals(observationSearchQuery);
        if (vitalRows.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> vitalsUuids = vitalRows.stream().map(VitalsDBRecord::getVitalsUuid).toList();
        List<VitalsUuidMappingDBRecord> mappings = vitalService.findVitalsUuidMappings(vitalsUuids);

        Map<UUID, List<VitalsUuidMappingDBRecord>> mappingsByVitalsUuid =
                mappings.stream().collect(Collectors.groupingBy(VitalsUuidMappingDBRecord::getTargetUuid));

        boolean hasCodeFilter =
                observationSearchQuery.getCodes() != null && !observationSearchQuery.getCodes().isEmpty();

        Set<String> requestedCodes = hasCodeFilter
                ? observationSearchQuery.getCodes().stream()
                .map(c -> c.getValue())
                .collect(Collectors.toSet())
                : Collections.emptySet();

        return vitalRows.stream()
                .flatMap(row -> {
                    List<VitalsUuidMappingDBRecord> rowMappings =
                            mappingsByVitalsUuid.getOrDefault(row.getVitalsUuid(), List.of());

                    if (hasCodeFilter) {
                        rowMappings = rowMappings.stream()
                                .filter(mapping -> requestedCodes.contains(
                                        ObservationUuidUtil.getCode(mapping.getResourcePath())))
                                .toList();
                    }

                    return vitalObservationAssembler.toCanonical(row, rowMappings).stream();
                })
                .toList();
    }
}
