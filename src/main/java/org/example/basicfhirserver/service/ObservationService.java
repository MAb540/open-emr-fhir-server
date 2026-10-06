package org.example.basicfhirserver.service;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;

public interface ObservationService {

  VitalObservation findById(UUID uuid);

  List<VitalObservation> find(ObservationSearchQuery observationSearchQuery);
}
