package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Extension;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;


@Component
public class DiagnosticReportProcedureMapperImpl implements DiagnosticReportProcedureMapper {
    @Override
    public DiagnosticReport toR4(ProcedureDBRecord procedureDBRecord) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();

        diagnosticReport.setMeta(populateMeta());
        diagnosticReport.setId(procedureDBRecord.getUuid().toString());

        if (procedureDBRecord.getEncounter().getDate() != null) {
            diagnosticReport.getEffectiveDateTimeType()
                    .setValue(Date.from(procedureDBRecord.getEncounter().getDate().atZone(ZoneId.systemDefault()).toInstant()));
            diagnosticReport.setIssued(
                    Date.from(procedureDBRecord.getEncounter().getDate().atZone(ZoneId.systemDefault()).toInstant())
            );
        } else {
            diagnosticReport.getEffectiveDateTimeType()
                    .setExtension(
                            List.of(new Extension().setValue(getUnknownCodeableConcept()))
                    );

        }

        return diagnosticReport;
    }


    private org.hl7.fhir.r4.model.Meta populateMeta() {
        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-lab");

        return meta;
    }
}
