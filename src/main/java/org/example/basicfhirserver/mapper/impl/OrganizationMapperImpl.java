package org.example.basicfhirserver.mapper.impl;

import java.util.ArrayList;
import java.util.List;
import org.example.basicfhirserver.mapper.OrganizationMapper;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityDBRecord;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapperImpl implements OrganizationMapper {

  @Override
  public Organization toR4(FacilityDBRecord facilityDBRecord) {
    Organization organization = new Organization();
    organization.getMeta().addProfile(ProfilesConstants.HL7_US_CORE_ORGANIZATION);
    organization.getMeta().setVersionId("1");

    organization.setId(facilityDBRecord.getUuid().toString());
    organization.setActive(
        facilityDBRecord.getInactive() == null || facilityDBRecord.getInactive() == 0);

    if (facilityDBRecord.getName() != null) {
      organization.setName(facilityDBRecord.getName());
    }

    if (facilityDBRecord.getFacilityNpi() != null) {
      organization.addIdentifier(
          new Identifier()
              .setSystem("http://hl7.org/fhir/sid/us-npi")
              .setValue(facilityDBRecord.getFacilityNpi()));
    }

    addTelecom(organization, facilityDBRecord);
    addAddress(organization, facilityDBRecord);

    return organization;
  }

  private void addTelecom(Organization organization, FacilityDBRecord facilityDBRecord) {
    if (facilityDBRecord.getPhone() != null) {
      organization.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.PHONE)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(facilityDBRecord.getPhone()));
    }
    if (facilityDBRecord.getFax() != null) {
      organization.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.FAX)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(facilityDBRecord.getFax()));
    }
    if (facilityDBRecord.getEmail() != null) {
      organization.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.EMAIL)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(facilityDBRecord.getEmail()));
    }
    if (facilityDBRecord.getWebsite() != null) {
      organization.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.URL)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(facilityDBRecord.getWebsite()));
    }
  }

  private void addAddress(Organization organization, FacilityDBRecord facilityDBRecord) {
    List<StringType> lines = new ArrayList<>();
    if (facilityDBRecord.getStreet() != null
        && !facilityDBRecord.getStreet().trim().isEmpty()) {
      lines.add(new StringType(facilityDBRecord.getStreet()));
    }

    boolean hasAddress =
        !lines.isEmpty()
            || facilityDBRecord.getCity() != null
            || facilityDBRecord.getState() != null
            || facilityDBRecord.getPostalCode() != null
            || facilityDBRecord.getCountryCode() != null;

    if (!hasAddress) {
      return;
    }

    Address address = new Address().setUse(Address.AddressUse.WORK);
    if (!lines.isEmpty()) {
      address.setLine(lines);
    }
    address.setCity(facilityDBRecord.getCity());
    address.setState(facilityDBRecord.getState());
    address.setPostalCode(facilityDBRecord.getPostalCode());
    address.setCountry(facilityDBRecord.getCountryCode());

    organization.addAddress(address);
  }
}
