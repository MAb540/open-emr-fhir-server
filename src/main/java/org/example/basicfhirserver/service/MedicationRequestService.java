package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.MedicationRequest;

public interface MedicationRequestService {

  MedicationRequest findById(UUID uuid);

  SearchResult search(MedicationRequestSearchQuery medicationRequestSearchQuery);
}
