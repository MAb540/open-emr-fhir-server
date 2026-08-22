package org.example.basicfhirserver.mapper;


import org.example.basicfhirserver.model.Practitioner;

public interface PractitionerMapper extends ResourceMapper<org.hl7.fhir.r4.model.Practitioner, Practitioner> {

    org.hl7.fhir.r4.model.Practitioner toR4(Practitioner practitioner);

}
