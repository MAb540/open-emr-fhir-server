package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.springframework.data.domain.Page;

public interface PatientService {

  LegacyPatientEntity findById(UUID uuid);

  Page<LegacyPatientEntity> find(PatientSearchQuery query);
}
