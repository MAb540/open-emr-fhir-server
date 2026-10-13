package org.example.basicfhirserver.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;

public interface ObservationService {

  Optional<CanonicalObservation> findById(UUID uuid);

  List<CanonicalObservation> find(ObservationSearchQuery observationSearchQuery);
}
