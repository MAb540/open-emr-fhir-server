package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.hl7.fhir.r4.model.Patient;

public interface LegacyPatientMapper extends ResourceMapper<Patient, LegacyPatientEntity> {

    Patient toR4(LegacyPatientEntity legacyPatientEntity);
}
