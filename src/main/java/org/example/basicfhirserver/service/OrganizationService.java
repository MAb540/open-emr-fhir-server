package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Organization;

public interface OrganizationService {

  Organization findById(UUID uuid);

  SearchResult search(OrganizationSearchQuery organizationSearchQuery);
}
