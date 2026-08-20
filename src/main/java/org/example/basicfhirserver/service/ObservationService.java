package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;

import java.util.List;
import java.util.UUID;

public interface ObservationService {

    VitalObservation findById(UUID uuid);

    List<VitalObservation> find(ObservationSearchQuery observationSearchQuery);

}
