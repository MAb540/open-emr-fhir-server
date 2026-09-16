package org.example.basicfhirserver.service.impl;

import org.example.basicfhirserver.model.DiagnosticReportCanonical;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesRepository;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureRepository;
import org.example.basicfhirserver.service.DiagnosticReportService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DiagnosticServiceImpl implements DiagnosticReportService {

    private final ClinicalNotesRepository clinicalNotesRepository;
    private final ProcedureRepository procedureRepository;

    public DiagnosticServiceImpl(ClinicalNotesRepository clinicalNotesRepository,
                                 ProcedureRepository procedureRepository) {
        this.clinicalNotesRepository = clinicalNotesRepository;
        this.procedureRepository = procedureRepository;
    }


    @Override
    public ClinicalNotesDBRecord findClinicalNotesById(UUID uuid) {
        return null;
    }

    @Override
    public DiagnosticReportCanonical findClinicalNotes(DiagnosticReportSearchQuery diagnosticReportSearchQuery) {

        List<ClinicalNotesDBRecord> clinicalNotesDBRecords = clinicalNotesRepository.findClinicalNotes(diagnosticReportSearchQuery);
        List<ProcedureDBRecord> procedureDBRecords = procedureRepository.findProcedures(diagnosticReportSearchQuery);

        return new
                DiagnosticReportCanonical(
                clinicalNotesDBRecords,
                procedureDBRecords
        );
    }
}
