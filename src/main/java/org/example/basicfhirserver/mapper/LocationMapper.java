package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.location.LocationDBRecord;
import org.hl7.fhir.r4.model.Location;

public interface LocationMapper extends ResourceMapper<Location, LocationDBRecord> {

  Location toR4(LocationDBRecord locationDBRecord);
}
