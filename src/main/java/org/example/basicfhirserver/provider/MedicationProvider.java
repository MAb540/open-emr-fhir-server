package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.MedicationMapper;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.MedicationSearchTranslator;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.example.basicfhirserver.service.MedicationService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MedicationProvider implements IResourceProvider {

    private final MedicationService medicationService;
    private final MedicationMapper medicationMapper;
    private final MedicationSearchTranslator medicationSearchTranslator;

    public MedicationProvider(MedicationService medicationService,
                              MedicationMapper medicationMapper,
                              MedicationSearchTranslator medicationSearchTranslator){
        this.medicationService = medicationService;
        this.medicationMapper = medicationMapper;
        this.medicationSearchTranslator = medicationSearchTranslator;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Medication.class;
    }

    @Read()
    public Medication getResourceById(@IdParam IdType theId) {
        DrugDBRecord drugDBRecord = medicationService.findById(UUID.fromString(theId.getIdPart()));
        return medicationMapper.toR4(drugDBRecord);
    }

    @Search()
    public IBundleProvider searchMedication(
            @Count Integer count,
            @Offset Integer offset
    ) {

        MedicationSearchCriteria medicationSearchCriteria = MedicationSearchCriteria.builder()
                .count(count)
                .offset(offset)
                .build();

        var medicationSearchQuery = medicationSearchTranslator.translate(medicationSearchCriteria);

        Page<DrugDBRecord> drugDBRecords =
                medicationService.find(medicationSearchQuery);

        List<IBaseResource> primaryMedications = drugDBRecords.getContent().stream()
                .<IBaseResource>map(medicationMapper::toR4)
                .toList();

        int currentOffset = offset != null ? offset : 0;
        int currentPageSize = drugDBRecords.getContent().size();

        return new BundleProvider(
                primaryMedications,
                List.of(),
                Math.toIntExact(drugDBRecords.getTotalElements()),
                currentOffset,
                currentPageSize
        );
    }


}
