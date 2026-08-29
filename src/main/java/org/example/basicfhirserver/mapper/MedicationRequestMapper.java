package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.hl7.fhir.r4.model.MedicationRequest;

public interface MedicationRequestMapper extends ResourceMapper<MedicationRequest, PrescriptionDBRecord> {

    MedicationRequest toR4(PrescriptionDBRecord prescriptionDBRecord);

}
