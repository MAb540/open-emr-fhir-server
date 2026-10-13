package org.example.basicfhirserver.service.impl.observation;

import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObservationProvider {

  boolean supports(ObservationSearchQuery query);

  Optional<CanonicalObservation> fetchById(UUID uuid);

  List<CanonicalObservation> fetch(ObservationSearchQuery query);
}
