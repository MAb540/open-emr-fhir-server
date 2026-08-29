package org.example.basicfhirserver.mapper.utils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class MapperHelper {

    public static CodeableConcept getUnknownCodeableConcept() {
        CodeableConcept unknownConcept = new CodeableConcept();
        Coding unknownCoding = new Coding();
        unknownCoding.setSystem("http://terminology.hl7.org/CodeSystem/data-absent-reason");
        unknownCoding.setCode("unknown");
        unknownCoding.setDisplay("Unknown");
        unknownConcept.addCoding(unknownCoding);
        return unknownConcept;
    }

}
