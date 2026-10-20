package org.example.basicfhirserver.service.impl.diagnosticreport;

import java.util.ArrayList;
import java.util.List;
import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesRepository;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureDBRecord;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureRepository;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticReportServiceImpl implements DiagnosticReportService {

  private final ClinicalNotesRepository clinicalNotesRepository;
  private final ProcedureRepository procedureRepository;
  private final DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper;
  private final DiagnosticReportProcedureMapper diagnosticReportProcedureMapper;

  public DiagnosticReportServiceImpl(
      ClinicalNotesRepository clinicalNotesRepository,
      ProcedureRepository procedureRepository,
      DiagnosticReportClinicalNotesMapper diagnosticReportClinicalNotesMapper,
      DiagnosticReportProcedureMapper diagnosticReportProcedureMapper) {
    this.clinicalNotesRepository = clinicalNotesRepository;
    this.procedureRepository = procedureRepository;
    this.diagnosticReportClinicalNotesMapper = diagnosticReportClinicalNotesMapper;
    this.diagnosticReportProcedureMapper = diagnosticReportProcedureMapper;
  }

  @Override
  public SearchResult search(DiagnosticReportSearchQuery diagnosticReportSearchQuery) {
    Page<ClinicalNotesDBRecord> notes =
        clinicalNotesRepository.findClinicalNotes(diagnosticReportSearchQuery);
    Page<ProcedureDBRecord> procedures =
        procedureRepository.findProcedures(diagnosticReportSearchQuery);

    List<IBaseResource> primaryResources = new ArrayList<>();
    notes.getContent().stream()
        .map(diagnosticReportClinicalNotesMapper::toR4)
        .forEach(primaryResources::add);
    procedures.getContent().stream()
        .map(diagnosticReportProcedureMapper::toR4)
        .forEach(primaryResources::add);

    int totalElements = (int) (notes.getTotalElements() + procedures.getTotalElements());
    int currentOffset =
        diagnosticReportSearchQuery.getOffset() != null
            ? diagnosticReportSearchQuery.getOffset()
            : 0;

    return new SearchResult(primaryResources, List.of(), totalElements, currentOffset);
  }
}
