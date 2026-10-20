package org.example.basicfhirserver.service.impl.organization;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.OrganizationMapper;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityDBRecord;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityRepository;
import org.example.basicfhirserver.service.OrganizationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final FacilityRepository facilityRepository;
  private final OrganizationMapper organizationMapper;

  public OrganizationServiceImpl(
      FacilityRepository facilityRepository, OrganizationMapper organizationMapper) {
    this.facilityRepository = facilityRepository;
    this.organizationMapper = organizationMapper;
  }

  @Override
  public Organization findById(UUID uuid) {
    List<FacilityDBRecord> facilityDBRecords = facilityRepository.findById(uuid);
    if (facilityDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Organization with given ID " + uuid + " not found.");
    }
    return organizationMapper.toR4(facilityDBRecords.get(0));
  }

  @Override
  public SearchResult search(OrganizationSearchQuery organizationSearchQuery) {
    Page<FacilityDBRecord> facilityDBRecords = facilityRepository.find(organizationSearchQuery);

    List<IBaseResource> primaryOrganizations =
        facilityDBRecords.getContent().stream()
            .<IBaseResource>map(organizationMapper::toR4)
            .toList();

    int currentOffset =
        organizationSearchQuery.getOffset() != null ? organizationSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryOrganizations, List.of(), (int) facilityDBRecords.getTotalElements(), currentOffset);
  }
}
