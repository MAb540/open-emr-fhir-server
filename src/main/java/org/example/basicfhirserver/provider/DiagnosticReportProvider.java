package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.model.DiagnosticReportCanonical;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.DiagnosticReportSearchTranslator;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiagnosticReportProvider implements IResourceProvider {

    private final DiagnosticReportService diagnosticReportService;
    private final DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper;
    private final DiagnosticReportProcedureMapper diagnosticReportProcedureMapper;
    private final DiagnosticReportSearchTranslator diagnosticReportSearchTranslator;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return DiagnosticReport.class;
    }

    private DiagnosticReportProvider(DiagnosticReportService diagnosticReportService,
                                     DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper,
                                     DiagnosticReportProcedureMapper diagnosticReportProcedureMapper,
                                     DiagnosticReportSearchTranslator diagnosticReportSearchTranslator
    ) {
        this.diagnosticReportService = diagnosticReportService;
        this.diagnosticReportClinicalNotesMapper = diagnosticReportClinicalNotesMapper;
        this.diagnosticReportProcedureMapper = diagnosticReportProcedureMapper;
        this.diagnosticReportSearchTranslator = diagnosticReportSearchTranslator;
    }


    @Search()
    public IBundleProvider searchDiagnosticReport(
            @OptionalParam(name = DiagnosticReport.SP_RES_ID) TokenParam id,
            @OptionalParam(name = DiagnosticReport.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = Observation.SP_DATE) DateParam date,
            @OptionalParam(name = Observation.SP_CODE) TokenOrListParam codes,
            @Count Integer count,
            @Offset Integer offset
    ) {
        DiagnosticReportSearchCriteria diagnosticReportSearchCriteria = DiagnosticReportSearchCriteria.builder()
                .id(id)
                .patient(patient)
                .date(date)
                .codes(codes)
                .count(count)
                .offset(offset)
                .build();

        var diagnosticReportSearchQuery = diagnosticReportSearchTranslator.translate(diagnosticReportSearchCriteria);
        DiagnosticReportCanonical diagnosticReports = diagnosticReportService.findClinicalNotes(diagnosticReportSearchQuery);

        Page<ClinicalNotesDBRecord> notes = diagnosticReports.getNotes();
        List<IBaseResource> notesResources = notes.getContent().stream()
                .<IBaseResource>map(diagnosticReportClinicalNotesMapper::toR4)
                .toList();

        Page<ProcedureDBRecord> procedures = diagnosticReports.getProcedures();
        List<IBaseResource> proceduresResources = procedures.getContent().stream()
                .<IBaseResource>map(diagnosticReportProcedureMapper::toR4)
                .toList();

        List<IBaseResource> combinedResources = new ArrayList<>();
        combinedResources.addAll(notesResources);
        combinedResources.addAll(proceduresResources);

        int totalCombinedElements = Math.toIntExact(notes.getTotalElements() + procedures.getTotalElements());

        SimpleBundleProvider bundleProvider = new SimpleBundleProvider(combinedResources);
        bundleProvider.setSize(totalCombinedElements);

        return bundleProvider;
    }


}
