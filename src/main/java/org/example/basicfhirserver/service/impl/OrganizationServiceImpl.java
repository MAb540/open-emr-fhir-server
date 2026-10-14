package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityDBRecord;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityRepository;
import org.example.basicfhirserver.service.OrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final FacilityRepository facilityRepository;

  public OrganizationServiceImpl(FacilityRepository facilityRepository) {
    this.facilityRepository = facilityRepository;
  }

  @Override
  public FacilityDBRecord findById(UUID uuid) {
    List<FacilityDBRecord> facilityDBRecords = facilityRepository.findById(uuid);
    if (facilityDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Organization with given ID " + uuid + " not found.");
    }
    return facilityDBRecords.get(0);
  }

  @Override
  public Page<FacilityDBRecord> find(OrganizationSearchQuery organizationSearchQuery) {
    return facilityRepository.find(organizationSearchQuery);
  }
}
