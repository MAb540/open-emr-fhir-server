package org.example.basicfhirserver.mapper.impl;

import java.util.ArrayList;
import java.util.List;
import org.example.basicfhirserver.mapper.PersonMapper;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class PersonMapperImpl implements PersonMapper {

  @Override
  public Person toR4(UserDBRecord userDBRecord) {
    Person person = new Person();
    person.setId(userDBRecord.getUuid().toString());

    if (userDBRecord.getActive() != null) {
      person.setActive(userDBRecord.getActive() == 1);
    }

    if (userDBRecord.getLname() != null
        || userDBRecord.getFname() != null
        || userDBRecord.getMname() != null
        || userDBRecord.getTitle() != null) {
      person.addName(createHumanName(userDBRecord));
    }

    addTelecom(person, userDBRecord);
    addAddress(person, userDBRecord);

    return person;
  }

  private HumanName createHumanName(UserDBRecord userDBRecord) {
    HumanName name = new HumanName();
    name.setUse(HumanName.NameUse.OFFICIAL);

    if (userDBRecord.getTitle() != null && !userDBRecord.getTitle().trim().isEmpty()) {
      name.addPrefix(userDBRecord.getTitle());
    }
    if (userDBRecord.getLname() != null && !userDBRecord.getLname().trim().isEmpty()) {
      name.setFamily(userDBRecord.getLname());
    }
    if (userDBRecord.getFname() != null && !userDBRecord.getFname().trim().isEmpty()) {
      name.addGiven(userDBRecord.getFname());
    }
    if (userDBRecord.getMname() != null && !userDBRecord.getMname().trim().isEmpty()) {
      name.addGiven(userDBRecord.getMname());
    }

    return name;
  }

  private void addTelecom(Person person, UserDBRecord userDBRecord) {
    if (userDBRecord.getPhone() != null) {
      person.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.PHONE)
              .setValue(userDBRecord.getPhone()));
    }
    if (userDBRecord.getFax() != null) {
      person.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.FAX)
              .setValue(userDBRecord.getFax()));
    }
    if (userDBRecord.getEmail() != null) {
      person.addTelecom(
          new ContactPoint()
              .setSystem(ContactPoint.ContactPointSystem.EMAIL)
              .setValue(userDBRecord.getEmail()));
    }
  }

  private void addAddress(Person person, UserDBRecord userDBRecord) {
    List<StringType> lines = new ArrayList<>();
    if (userDBRecord.getStreet() != null && !userDBRecord.getStreet().trim().isEmpty()) {
      lines.add(new StringType(userDBRecord.getStreet()));
    }
    if (userDBRecord.getStreetb() != null && !userDBRecord.getStreetb().trim().isEmpty()) {
      lines.add(new StringType(userDBRecord.getStreetb()));
    }

    boolean hasAddress =
        !lines.isEmpty()
            || userDBRecord.getCity() != null
            || userDBRecord.getState() != null
            || userDBRecord.getZip() != null;

    if (!hasAddress) {
      return;
    }

    Address address = new Address().setUse(Address.AddressUse.WORK);
    if (!lines.isEmpty()) {
      address.setLine(lines);
    }
    address.setCity(userDBRecord.getCity());
    address.setState(userDBRecord.getState());
    address.setPostalCode(userDBRecord.getZip());

    person.addAddress(address);
  }
}
