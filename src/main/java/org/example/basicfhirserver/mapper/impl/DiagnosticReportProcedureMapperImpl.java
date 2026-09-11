package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;


@Component
public class DiagnosticReportProcedureMapperImpl implements DiagnosticReportProcedureMapper {
    @Override
    public DiagnosticReport toR4(ProcedureDBRecord procedureDBRecord) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();

        diagnosticReport.setMeta(populateMeta());
        diagnosticReport.setId(procedureDBRecord.getUuid().toString());

        if(diagnosticReport.getEffectiveDateTimeType()){
            diagnosticReport.getEffectiveDateTimeType()
                            .setValue(Date.from(procedureDBRecord.getDate.atZone(ZoneId.systemDefault()).toInstant()));
            procedureDBRecord.setIssued();
        }else{

        }

//
//        if (!empty($dataRecordReport['date'])) {
//            $utcDate = UtilsService::getLocalDateAsUTC($dataRecordReport['date']);
//            $report->setEffectiveDateTime(new FHIRDateTime($utcDate));
//            $report->setIssued(new FHIRInstant($utcDate));
//        } else {
//            $report->setEffectiveDateTime(UtilsService::createDataMissingExtension());
//        }



        return diagnosticReport;
    }


    private org.hl7.fhir.r4.model.Meta populateMeta() {
        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-lab");

        return meta;
    }
}
