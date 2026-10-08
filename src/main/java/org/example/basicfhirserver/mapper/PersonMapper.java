package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.hl7.fhir.r4.model.Person;

public interface PersonMapper extends ResourceMapper<Person, UserDBRecord> {

  Person toR4(UserDBRecord userDBRecord);
}
