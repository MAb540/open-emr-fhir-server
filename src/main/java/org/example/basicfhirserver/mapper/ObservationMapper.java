package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.model.VitalObservation;
import org.hl7.fhir.r4.model.Observation;

public interface ObservationMapper extends ResourceMapper<Observation, VitalObservation> {

    Observation toR4(VitalObservation vitalObservation);
}
