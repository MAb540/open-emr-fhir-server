package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.DiagnosticReportProcedureMapper;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.repository.jdbc.diagnosticreport.ProcedureDBRecord;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
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
        diagnosticReport.setEncounter(populateEncounter(procedureDBRecord));
        diagnosticReport.setPerformer(populatePerformer(procedureDBRecord));
        diagnosticReport.setResult(populateResult(procedureDBRecord));
        diagnosticReport.setSubject(populatePatient(procedureDBRecord));
        diagnosticReport.setCategory(populateCategory());
        diagnosticReport.setBasedOn(populateBasedOn(procedureDBRecord));
        diagnosticReport.setResultsInterpreter(populateResultsInterpreter(procedureDBRecord));
        diagnosticReport.setStatus(populateStatus(procedureDBRecord));
        diagnosticReport.setCode(populateCode(procedureDBRecord));

        return diagnosticReport;
    }

    private org.hl7.fhir.r4.model.Meta populateMeta() {
        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-diagnosticreport-lab");
        return meta;
    }

    private Reference populateEncounter(
            ProcedureDBRecord procedureDBRecord) {
        Reference encounterReference = new Reference();
        if (procedureDBRecord.getEncounter().getUuid() != null) {
            encounterReference.setReference("Encounter/" + procedureDBRecord.getEncounter().getUuid());
        }
        return encounterReference;
    }

    private List<Reference> populatePerformer(
            ProcedureDBRecord procedureDBRecord) {
        if (procedureDBRecord.getLab() == null) {
            return List.of();
        }

        Reference organizationReference = new Reference();
        if (procedureDBRecord.getLab().getUuid() != null) {
            organizationReference.setReference("Organization/" + procedureDBRecord.getLab().getUuid());
        }

        Reference practitionerReference = new Reference();
        if (procedureDBRecord.getLab().getDirectorUuid() != null) {
            practitionerReference.setReference("Practitioner/" + procedureDBRecord.getLab().getDirectorUuid());
        }
        return List.of(organizationReference, practitionerReference);
    }

    private List<Reference> populateResult(
            ProcedureDBRecord procedureDBRecord) {

        if (procedureDBRecord.getReports() == null) {
            return List.of();
        }

        List<Reference> results = new ArrayList<>();
        for (ProcedureDBRecord.ReportBlock reportBlock : procedureDBRecord.getReports()) {
            if (reportBlock.getResults() != null && !reportBlock.getResults().isEmpty()) {
                for (ProcedureDBRecord.ResultBlock result : reportBlock.getResults()) {
                    Reference obsReference = new Reference("Observation/" + result.getUuid());
                    if (result.getText() != null && !result.getText().trim().isEmpty()) {
                        obsReference.setDisplay(result.getText());
                    }
                    results.add(obsReference);
                }
            }
        }
        return results;
    }

    private Reference populatePatient(
            ProcedureDBRecord procedureDBRecord) {

        Reference patientReference = new Reference();
        if (procedureDBRecord.getPatient().getUuid() != null) {
            patientReference.setReference("Patient/" + procedureDBRecord.getPatient().getUuid());
        }
        return patientReference;
    }

    private List<CodeableConcept> populateCategory() {
        CodeableConcept codeableConcept = new CodeableConcept();
        String LAB_CATEGORY = "LAB_CATEGORY";

        Coding coding = new Coding();
        coding.setSystem(FhirCodeSystemConstants.DIAGNOSTIC_SERVICE_SECTION_ID);
        coding.setCode(LAB_CATEGORY);
        coding.setDisplay("Laboratory");
        codeableConcept.setText("Laboratory");
        codeableConcept.addCoding(coding);
        return List.of(codeableConcept);
    }

    private List<Reference> populateBasedOn(
            ProcedureDBRecord procedureDBRecord) {
        if (procedureDBRecord.getOrderUuid() == null) {
            return List.of();
        }
        Reference basedOnReference = new Reference();
        basedOnReference.setReference("ServiceRequest/" + procedureDBRecord.getOrderUuid());
        return List.of(basedOnReference);
    }

    private List<Reference> populateResultsInterpreter(
            ProcedureDBRecord procedureDBRecord) {
        if (procedureDBRecord.getProvider().getUuid() == null) {
            return List.of();
        }
        Reference practitionerReference = new Reference();
        practitionerReference.setReference("Practitioner/" + procedureDBRecord.getProvider().getUuid());
        return List.of(practitionerReference);
    }

    private DiagnosticReport.DiagnosticReportStatus populateStatus(ProcedureDBRecord procedureDBRecord) {
        if (procedureDBRecord.getOrderStatus() != null) {
            if (procedureDBRecord.getOrderStatus().equals("completed")) {
                return DiagnosticReport.DiagnosticReportStatus.FINAL;
            } else if (procedureDBRecord.getOrderStatus().equals("received")) {
                return DiagnosticReport.DiagnosticReportStatus.REGISTERED;
            }
        }
        return DiagnosticReport.DiagnosticReportStatus.FINAL;
    }

    private CodeableConcept populateCode(ProcedureDBRecord procedureDBRecord) {

        CodeableConcept codeableConcept = new CodeableConcept();
        if (procedureDBRecord.getStandardCode() != null) {
            Coding coding = new Coding();

            coding.setCode(procedureDBRecord.getStandardCode());
            coding.setSystem(FhirCodeSystemConstants.LOINC);
            coding.setDisplay(procedureDBRecord.getProcedureName());

            codeableConcept.setText(procedureDBRecord.getProcedureName());
            codeableConcept.addCoding(coding);
        } else {
            String UNKNOWNABLE_CODE_NULL_FLAVOR = "UNK";
            Coding coding = new Coding();
            coding.setSystem(FhirCodeSystemConstants.HL7_NULL_FLAVOR);
            coding.setCode(UNKNOWNABLE_CODE_NULL_FLAVOR);
            coding.setDisplay("unknown");
            codeableConcept.setText("unknown");
            codeableConcept.addCoding(coding);
        }
        return codeableConcept;
    }

}
