package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.hl7.fhir.r4.model.Medication;

public interface MedicationMapper extends ResourceMapper<Medication, DrugDBRecord> {

    Medication toR4(DrugDBRecord drugDBRecord);

}
