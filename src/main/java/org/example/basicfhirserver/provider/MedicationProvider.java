package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.MedicationMapper;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.example.basicfhirserver.service.MedicationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MedicationProvider implements IResourceProvider {

    private final MedicationService medicationService;
    private final MedicationMapper medicationMapper;

    public MedicationProvider(MedicationService medicationService,
                              MedicationMapper medicationMapper){
        this.medicationService = medicationService;
        this.medicationMapper = medicationMapper;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Medication.class;
    }

    @Search()
    public List<Medication> searchEncounters(
    ) {
        List<DrugDBRecord> drugDBRecords = medicationService.find();

        return drugDBRecords.stream()
                .map(medicationMapper::toR4)
                .toList();
    }


}
