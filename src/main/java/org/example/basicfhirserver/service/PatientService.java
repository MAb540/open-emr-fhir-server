package org.example.basicfhirserver.service;

import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PatientService {

    LegacyPatientEntity findById(UUID uuid);

    Page<LegacyPatientEntity> find(PatientSearchQuery query);

}
