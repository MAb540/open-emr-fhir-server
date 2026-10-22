package org.example.basicfhirserver.mapper.impl;

import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.example.basicfhirserver.mapper.LocationMapper;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.mapper.utils.PlaceOfService;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.repository.jdbc.location.LocationDBRecord;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class LocationMapperImpl implements LocationMapper {

  private static final String HOME_ADDRESS_NAME = "Home Address";
  private static final String FACILITY_TYPE = "FACILITY";

  @Override
  public Location toR4(LocationDBRecord locationDBRecord) {
    Location location = new Location();

    location.setMeta(populateMeta(locationDBRecord));

    location.setId(
        locationDBRecord.getFhirLocationUuid() != null
            ? locationDBRecord.getFhirLocationUuid().toString()
            : locationDBRecord.getTableUuid().toString());

    location.setStatus(Location.LocationStatus.ACTIVE);
    location.setNameElement(populateName(locationDBRecord));

    Identifier identifier = populateIdentifier(locationDBRecord);
    if (identifier != null) {
      location.addIdentifier(identifier);
    }

    CodeableConcept serviceRoleType = populateServiceRoleType(locationDBRecord);
    if (serviceRoleType != null) {
      location.addType(serviceRoleType);
    }

    if (shouldIncludeContactInformation(locationDBRecord)) {
      addTelecom(location, locationDBRecord);
      addAddress(location, locationDBRecord);
    }

    return location;
  }

  private Meta populateMeta(LocationDBRecord locationDBRecord) {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.addProfile(ProfilesConstants.HL7_US_CORE_LOCATION);
    meta.setLastUpdated(
        locationDBRecord.getLastUpdated() != null
            ? toDate(locationDBRecord.getLastUpdated())
            : Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
    return meta;
  }

  private StringType populateName(LocationDBRecord locationDBRecord) {
    String name = locationDBRecord.getName();
    if (name == null || name.isEmpty()) {
      StringType nameElement = new StringType();
      nameElement.addExtension(
          new Extension(FhirCodeSystemConstants.DATA_ABSENT_REASON_EXTENSION)
              .setValue(getUnknownCodeableConcept()));
      return nameElement;
    }

    // Patient/user locations are hard-coded to the literal "Home Address" by the
    // LocationService query; only facilities carry a real name.
    if (!FACILITY_TYPE.equalsIgnoreCase(locationDBRecord.getType())) {
      return new StringType(HOME_ADDRESS_NAME);
    }
    return new StringType(name);
  }

  private Identifier populateIdentifier(LocationDBRecord locationDBRecord) {
    String identifierValue = locationDBRecord.getIdentifier();
    if (identifierValue == null || identifierValue.isEmpty()) {
      return null;
    }
    Identifier identifier = new Identifier().setValue(identifierValue);
    String system = getSystemForIdentifier(locationDBRecord.getIdentifierType());
    if (system != null) {
      identifier.setSystem(system);
    }
    return identifier;
  }

  private String getSystemForIdentifier(String identifierType) {
    if ("npi".equals(identifierType)) {
      return FhirCodeSystemConstants.PROVIDER_NPI;
    }
    return null;
  }

  private CodeableConcept populateServiceRoleType(LocationDBRecord locationDBRecord) {
    String locationRoleType = locationDBRecord.getLocationRoleType();
    if (locationRoleType == null || locationRoleType.isEmpty()) {
      return null;
    }

    String code = padToTwoDigits(locationRoleType);
    PlaceOfService placeOfService = PlaceOfService.fromCode(code);
    if (placeOfService == null) {
      return null;
    }

    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.CMS_PLACE_OF_SERVICE);
    coding.setCode(placeOfService.getCode());
    coding.setDisplay(placeOfService.getDisplay());
    return new CodeableConcept().addCoding(coding);
  }

  private String padToTwoDigits(String value) {
    return value.length() < 2 ? "0".repeat(2 - value.length()) + value : value;
  }

  private boolean shouldIncludeContactInformation(LocationDBRecord locationDBRecord) {
    return FACILITY_TYPE.equalsIgnoreCase(locationDBRecord.getType());
  }

  private void addTelecom(Location location, LocationDBRecord locationDBRecord) {
    if (locationDBRecord.getPhone() != null) {
      location.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.PHONE)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(locationDBRecord.getPhone()));
    }
    if (locationDBRecord.getFax() != null) {
      location.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.FAX)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(locationDBRecord.getFax()));
    }
    if (locationDBRecord.getEmail() != null) {
      location.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.EMAIL)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(locationDBRecord.getEmail()));
    }
    if (locationDBRecord.getWebsite() != null) {
      location.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.URL)
              .setUse(ContactPoint.ContactPointUse.WORK)
              .setValue(locationDBRecord.getWebsite()));
    }
  }

  private void addAddress(Location location, LocationDBRecord locationDBRecord) {
    List<StringType> lines = new ArrayList<>();
    if (locationDBRecord.getStreet() != null && !locationDBRecord.getStreet().trim().isEmpty()) {
      lines.add(new StringType(locationDBRecord.getStreet()));
    }

    boolean hasAddress =
        !lines.isEmpty()
            || locationDBRecord.getCity() != null
            || locationDBRecord.getState() != null
            || locationDBRecord.getPostalCode() != null
            || locationDBRecord.getCountryCode() != null;

    if (!hasAddress) {
      return;
    }

    Address address = new Address().setUse(Address.AddressUse.WORK);
    if (!lines.isEmpty()) {
      address.setLine(lines);
    }
    address.setCity(locationDBRecord.getCity());
    address.setState(locationDBRecord.getState());
    address.setPostalCode(locationDBRecord.getPostalCode());
    address.setCountry(locationDBRecord.getCountryCode());

    location.setAddress(address);
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
