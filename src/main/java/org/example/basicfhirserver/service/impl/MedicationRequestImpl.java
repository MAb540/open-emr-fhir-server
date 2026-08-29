package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.repository.jdbc.prescription.FacilityDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionService;
import org.example.basicfhirserver.service.MedicationRequestService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicationRequestImpl implements MedicationRequestService {

    private final PrescriptionService prescriptionService;

    public MedicationRequestImpl(PrescriptionService prescriptionService){
        this.prescriptionService = prescriptionService;
    }


    @Override
    public PrescriptionDBRecord findById(UUID uuid) {
        List<PrescriptionDBRecord> prescriptionDBRecords  = prescriptionService.findById(uuid);
        if(prescriptionDBRecords.isEmpty()){
            throw new ResourceNotFoundException("Medication Request with given ID " + uuid + " not found.");
        }
        return prescriptionDBRecords.get(0);
    }

    @Override
    public List<PrescriptionDBRecord> find(MedicationRequestSearchQuery medicationRequestSearchQuery) {
        List<PrescriptionDBRecord> prescriptionDBRecords = prescriptionService.find(medicationRequestSearchQuery);

        List<FacilityDBRecord> facilityDBRecords = prescriptionService.findFacility();
        if(!facilityDBRecords.isEmpty()){
            FacilityDBRecord facilityDBRecord = facilityDBRecords.get(0);
            prescriptionDBRecords
                    .forEach(prescription -> prescription.setOrganizationUuid(facilityDBRecord.getUuid()));
        }

        return prescriptionDBRecords;
    }
}
