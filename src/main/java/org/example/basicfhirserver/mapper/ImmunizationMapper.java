package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.immunization.ImmunizationDBRecord;
import org.hl7.fhir.r4.model.Immunization;

public interface ImmunizationMapper extends ResourceMapper<Immunization, ImmunizationDBRecord> {

  Immunization toR4(ImmunizationDBRecord immunizationDBRecord);
}
