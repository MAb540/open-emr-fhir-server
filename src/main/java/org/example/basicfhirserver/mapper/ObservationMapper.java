package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.model.CanonicalObservation;
import org.hl7.fhir.r4.model.Observation;

public interface ObservationMapper extends ResourceMapper<Observation, CanonicalObservation> {

  Observation toR4(CanonicalObservation canonicalObservation);
}
