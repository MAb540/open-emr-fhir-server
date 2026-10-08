package org.example.basicfhirserver.repository.jdbc.facility;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.springframework.data.domain.Page;

public interface FacilityService {

  List<FacilityDBRecord> findById(UUID uuid);

  Page<FacilityDBRecord> find(OrganizationSearchQuery organizationSearchQuery);
}
