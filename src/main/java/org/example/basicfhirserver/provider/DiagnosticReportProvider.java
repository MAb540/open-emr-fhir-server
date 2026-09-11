package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.model.DiagnosticReportCanonical;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DiagnosticReportProvider implements IResourceProvider {

    private final DiagnosticReportService diagnosticReportService;
    private final DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper;
    private final DiagnosticReportProcedureMapper diagnosticReportProcedureMapper;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return DiagnosticReport.class;
    }

    private DiagnosticReportProvider(DiagnosticReportService diagnosticReportService,
                                     DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper,
                                     DiagnosticReportProcedureMapper diagnosticReportProcedureMapper
    ) {
        this.diagnosticReportService = diagnosticReportService;
        this.diagnosticReportClinicalNotesMapper = diagnosticReportClinicalNotesMapper;
        this.diagnosticReportProcedureMapper = diagnosticReportProcedureMapper;
    }


    @Search()
    public List<DiagnosticReport> searchDiagnosticReport() {
        DiagnosticReportCanonical diagnosticReports = diagnosticReportService.findClinicalNotes();

        Stream<DiagnosticReport> clinicalNotesStream = diagnosticReports.getNotes().stream()
                .map(diagnosticReportClinicalNotesMapper::toR4);

        Stream<DiagnosticReport> procedureStream = diagnosticReports.getProcedures().stream()
                .map(diagnosticReportProcedureMapper::toR4);

        return Stream.concat(clinicalNotesStream, procedureStream)
                .collect(Collectors.toList());
    }


}
