package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityDBRecord;
import org.springframework.data.domain.Page;

public interface OrganizationService {

  FacilityDBRecord findById(UUID uuid);

  Page<FacilityDBRecord> find(OrganizationSearchQuery organizationSearchQuery);
}
