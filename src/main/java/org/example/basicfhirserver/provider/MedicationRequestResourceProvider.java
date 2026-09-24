package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.MedicationRequestMapper;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.MedicationRequestSearchTranslator;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.example.basicfhirserver.service.MedicationRequestService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MedicationRequestResourceProvider implements IResourceProvider {

    private final MedicationRequestService medicationRequestService;
    private final MedicationRequestMapper medicationRequestMapper;
    private final MedicationRequestSearchTranslator medicationRequestSearchTranslator;

    public MedicationRequestResourceProvider(
            MedicationRequestService medicationRequestService,
            MedicationRequestMapper medicationRequestMapper,
            MedicationRequestSearchTranslator medicationRequestSearchTranslator
    ) {
        this.medicationRequestService = medicationRequestService;
        this.medicationRequestMapper = medicationRequestMapper;
        this.medicationRequestSearchTranslator = medicationRequestSearchTranslator;
    }


    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return MedicationRequest.class;
    }


    @Read()
    public MedicationRequest getResourceById(@IdParam IdType theId) {
        PrescriptionDBRecord prescriptionDBRecord = medicationRequestService.findById(UUID.fromString(theId.getIdPart()));
        return medicationRequestMapper.toR4(prescriptionDBRecord);
    }

    @Search()
    public IBundleProvider searchMedicationRequest(
            @OptionalParam(name = MedicationRequest.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = MedicationRequest.SP_INTENT) TokenOrListParam intent,
            @OptionalParam(name = MedicationRequest.SP_STATUS) StringParam status,
            @Count Integer count,
            @Offset Integer offset
    ) {

        MedicationRequestSearchCriteria criteria = MedicationRequestSearchCriteria.builder()
                .patient(patient)
                .intent(intent)
                .status(status)
                .count(count)
                .offset(offset)
                .build();

        var medicationRequestSearchQuery = medicationRequestSearchTranslator.translate(criteria);
        Page<PrescriptionDBRecord> prescriptionDBRecords = medicationRequestService.find(medicationRequestSearchQuery);

        List<IBaseResource> primaryMedicationRequests = prescriptionDBRecords.getContent().stream()
                .<IBaseResource>map(medicationRequestMapper::toR4)
                .toList();

        int currentOffset = offset != null ? offset : 0;
        int currentPageSize = prescriptionDBRecords.getContent().size();

        return new BundleProvider(
                primaryMedicationRequests,
                List.of(),
                Math.toIntExact(prescriptionDBRecords.getTotalElements()),
                currentOffset,
                currentPageSize
        );
    }

}
