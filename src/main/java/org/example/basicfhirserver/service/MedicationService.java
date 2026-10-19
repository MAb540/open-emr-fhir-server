package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Medication;

public interface MedicationService {

  Medication findById(UUID uuid);

  SearchResult search(MedicationSearchQuery medicationSearchQuery);
}
