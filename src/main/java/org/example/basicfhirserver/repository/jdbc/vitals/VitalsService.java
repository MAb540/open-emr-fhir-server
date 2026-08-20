package org.example.basicfhirserver.repository.jdbc.vitals;

import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;

import java.util.List;
import java.util.UUID;

public interface VitalsService {

    List<VitalsUuidMappingDBRecord> findVitalsUuidMappings(List<UUID> vitalUuid);

    List<VitalsUuidMappingDBRecord> findVitalsUuidMappingsById(UUID uuid);

    List<VitalsDBRecord> findVitals(ObservationSearchQuery searchQuery);

    List<VitalsDBRecord> findVitalsById(UUID uuid);

}
