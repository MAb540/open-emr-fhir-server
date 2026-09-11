package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.DiagnosticReportClinicalNotesMapper;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ClinicalNotesDBRecord;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;

@Component
public class DiagnosticReportClinicalNotesMapperImpl implements DiagnosticReportClinicalNotesMapper {

    @Override
    public DiagnosticReport toR4(ClinicalNotesDBRecord clinicalNotesDBRecord) {

        DiagnosticReport diagnosticReport = new DiagnosticReport();

        diagnosticReport.setMeta(populateMeta(clinicalNotesDBRecord));
        diagnosticReport.setId(clinicalNotesDBRecord.getId().toString());

        return null;
    }

    private org.hl7.fhir.r4.model.Meta populateMeta(
            ClinicalNotesDBRecord clinicalNotesDBRecord) {
        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-note");

        if (clinicalNotesDBRecord.getLastUpdated() != null) {
            meta.setLastUpdated(
                    Date.from(clinicalNotesDBRecord.getLastUpdated().atZone(ZoneId.systemDefault()).toInstant())
            );
            return meta;
        }

        return meta;
    }

}
