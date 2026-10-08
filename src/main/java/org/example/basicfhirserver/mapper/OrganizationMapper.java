package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.facility.FacilityDBRecord;
import org.hl7.fhir.r4.model.Organization;

public interface OrganizationMapper extends ResourceMapper<Organization, FacilityDBRecord> {

  Organization toR4(FacilityDBRecord facilityDBRecord);
}
