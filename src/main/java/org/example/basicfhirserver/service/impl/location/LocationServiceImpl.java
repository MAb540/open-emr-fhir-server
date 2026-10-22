package org.example.basicfhirserver.service.impl.location;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.LocationMapper;
import org.example.basicfhirserver.query.resources.location.LocationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.location.LocationDBRecord;
import org.example.basicfhirserver.repository.jdbc.location.LocationRepository;
import org.example.basicfhirserver.service.LocationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements LocationService {

  private final LocationRepository locationRepository;
  private final LocationMapper locationMapper;

  public LocationServiceImpl(LocationRepository locationRepository, LocationMapper locationMapper) {
    this.locationRepository = locationRepository;
    this.locationMapper = locationMapper;
  }

  @Override
  public Location findById(UUID uuid) {
    List<LocationDBRecord> locationDBRecords = locationRepository.findById(uuid);
    if (locationDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Location with given ID " + uuid + " not found.");
    }
    return locationMapper.toR4(locationDBRecords.get(0));
  }

  @Override
  public SearchResult search(LocationSearchQuery locationSearchQuery) {
    Page<LocationDBRecord> locationDBRecords = locationRepository.find(locationSearchQuery);

    List<IBaseResource> primaryLocations =
        locationDBRecords.getContent().stream().<IBaseResource>map(locationMapper::toR4).toList();

    int currentOffset =
        locationSearchQuery.getOffset() != null ? locationSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryLocations, List.of(), (int) locationDBRecords.getTotalElements(), currentOffset);
  }
}
