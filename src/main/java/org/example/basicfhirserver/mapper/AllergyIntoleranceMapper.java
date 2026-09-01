package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.hl7.fhir.r4.model.AllergyIntolerance;

public interface AllergyIntoleranceMapper extends ResourceMapper<AllergyIntolerance, AllergyDBRecord> {

    AllergyIntolerance toR4(AllergyDBRecord allergyDBRecord);

}
