package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Patient;

public interface PatientService {

  Patient findById(UUID uuid);

  SearchResult search(PatientSearchQuery patientSearchQuery);
}
