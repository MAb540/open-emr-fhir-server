package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.model.DiagnosticReportCanonical;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.DiagnosticReportSearchTranslator;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public List<DiagnosticReport> searchDiagnosticReport(
            @OptionalParam(name = DiagnosticReport.SP_RES_ID) TokenParam id,
            @OptionalParam(name = DiagnosticReport.SP_PATIENT) ReferenceParam patient,
            @OptionalParam(name = Observation.SP_DATE) DateParam date,
            @OptionalParam(name = Observation.SP_CODE) TokenOrListParam codes
    ) {

        DiagnosticReportSearchCriteria diagnosticReportSearchCriteria = DiagnosticReportSearchCriteria.builder()
                .id(id)
                .patient(patient)
                .date(date)
                .codes(codes)
                .build();

        var diagnosticReportSearchQuery = diagnosticReportSearchTranslator.translate(diagnosticReportSearchCriteria);

        DiagnosticReportCanonical diagnosticReports = diagnosticReportService.findClinicalNotes(diagnosticReportSearchQuery);

        Stream<DiagnosticReport> clinicalNotesStream = diagnosticReports.getNotes().stream()
                .map(diagnosticReportClinicalNotesMapper::toR4);

        Stream<DiagnosticReport> procedureStream = diagnosticReports.getProcedures().stream()
                .map(diagnosticReportProcedureMapper::toR4);

        return Stream.concat(clinicalNotesStream, procedureStream)
                .collect(Collectors.toList());
    }


}
