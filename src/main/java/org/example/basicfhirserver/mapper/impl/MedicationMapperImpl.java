package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.MedicationMapper;
import org.example.basicfhirserver.mapper.utils.CodeTypes;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.example.basicfhirserver.mapper.utils.CodeTypes.getSystemForCodeType;
import static org.example.basicfhirserver.mapper.utils.CodeTypes.parseCode;

@Component
public class MedicationMapperImpl implements MedicationMapper {

    @Override
    public Medication toR4(DrugDBRecord drugDBRecord) {

        Medication medication = new Medication();
        medication.setMeta(populateMeta(drugDBRecord));
        medication.setText(populateNarrative());
        medication.setId(drugDBRecord.getUuid() != null ? drugDBRecord.getUuid().toString() : drugDBRecord.getDrugId().toString());
        medication.setStatus(drugDBRecord.getActive() == 1 ? Medication.MedicationStatus.ACTIVE :
                Medication.MedicationStatus.INACTIVE);

        medication.setCode(populateCode(drugDBRecord));
        medication.setForm(populateForm(drugDBRecord));
        medication.setBatch(populateBatch(drugDBRecord));

        return medication;
    }

    private org.hl7.fhir.r4.model.Meta populateMeta(
            DrugDBRecord drugDBRecord) {

        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest");
        if (drugDBRecord.getDrugLastUpdated() != null) {
            meta.setLastUpdated(
                    Date.from(drugDBRecord.getDrugLastUpdated().atZone(ZoneId.systemDefault()).toInstant())
            );
            return meta;
        }
        meta.setLastUpdated(
                Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
        );
        return meta;
    }

    private Narrative populateNarrative() {
        return new Narrative()
                .setStatus(Narrative.NarrativeStatus.GENERATED)
                .setDiv(new XhtmlNode(NodeType.Element, "div")
                        .setValue("Medication"));
    }

    private CodeableConcept populateCode(DrugDBRecord drugDBRecord) {

        CodeableConcept medicationCode = new CodeableConcept();
        String rxnormDrugCode = drugDBRecord.getRxnormDrugcode();
        String drugCode = drugDBRecord.getDrugCode();
        String drugName = drugDBRecord.getName();

        if (rxnormDrugCode != null && !rxnormDrugCode.trim().isEmpty()) {
            String diagnosticString;

            if (drugCode != null && drugCode.equals(rxnormDrugCode) && !drugCode.contains(":")) {
                diagnosticString = "RXCUI:" + drugCode;
            } else {
                diagnosticString = rxnormDrugCode;
            }


            Map<String, CodeDetails> codes = addCoding(diagnosticString);

            // Map each code entry directly to a clean HAPI FHIR Coding object
            for (Map.Entry<String, CodeDetails> entry : codes.entrySet()) {
                String codeKey = entry.getKey();
                CodeDetails details = entry.getValue();

                Coding fhirCoding = new Coding();
                fhirCoding.setCode(codeKey);
                fhirCoding.setSystem(details.getSystem());

                if (details.getDescription() == null || details.getDescription().trim().isEmpty()) {
                    fhirCoding.setDisplay(drugName);
                } else {
                    fhirCoding.setDisplay(details.getDescription());
                }

                medicationCode.addCoding(fhirCoding);
            }
        }

        return medicationCode;
    }


    private CodeableConcept populateForm(DrugDBRecord drugDBRecord) {

        record FormPair(String display, String code) {
        }
        Map<String, FormPair> FORM_LOOKUP = Map.ofEntries(
                Map.entry("1", new FormPair("suspension", "C60928")),
                Map.entry("2", new FormPair("tablet", "C42998")),
                Map.entry("3", new FormPair("capsule", "C25158")),
                Map.entry("4", new FormPair("solution", "C42986")),
                Map.entry("5", new FormPair("tsp", "C48544")),
                Map.entry("6", new FormPair("ml", "C28254")),
                Map.entry("7", new FormPair("units", "C44278")),
                Map.entry("8", new FormPair("inhalation", "C42944")),
                Map.entry("9", new FormPair("gtts(drops)", "C48491")),
                Map.entry("10", new FormPair("cream", "C28944")),
                Map.entry("11", new FormPair("ointment", "C42966")),
                Map.entry("12", new FormPair("puff", "C42944"))
        );

        FormPair result = FORM_LOOKUP.getOrDefault(drugDBRecord.getForm(), new FormPair("", ""));

        Coding code = new Coding();
        code.setSystem("http://ncimeta.nci.nih.gov");
        code.setCode(result.code());
        code.setDisplay(result.display());

        CodeableConcept formCode = new CodeableConcept();
        formCode.addCoding(code);

        return formCode;
    }

    private Medication.MedicationBatchComponent populateBatch(DrugDBRecord drugDBRecord) {

        LocalDateTime expirationDateTime = drugDBRecord.getExpiration();
        String lotNumber = drugDBRecord.getLotNumber();

        if (expirationDateTime == null && (lotNumber == null || lotNumber.trim().isEmpty())) {
            return null;
        }
        Medication.MedicationBatchComponent medicationBatchComponent = new Medication.MedicationBatchComponent();

        if (expirationDateTime != null) {
            Date expirationDate = Date.from(expirationDateTime.atZone(ZoneId.systemDefault()).toInstant());
            DateTimeType fhirDateTime = new DateTimeType(expirationDate);
            medicationBatchComponent.setExpirationDateElement(fhirDateTime);
        }

        if (lotNumber != null && !lotNumber.trim().isEmpty()) {
            medicationBatchComponent.setLotNumber(lotNumber);
        }

        return medicationBatchComponent;
    }

    public class CodeDetails {
        private final String code;
        private final String description;
        private final String codeType;
        private final String system;

        public CodeDetails(String code, String description, String codeType, String system) {
            this.code = code;
            this.description = description;
            this.codeType = codeType;
            this.system = system;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public String getCodeType() {
            return codeType;
        }

        public String getSystem() {
            return system;
        }
    }

    protected Map<String, CodeDetails> addCoding(String diagnosis) {
        Map<String, CodeDetails> diagnosisMap = new HashMap<>();

        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            return diagnosisMap;
        }

        String[] diags = diagnosis.split(";");

        for (String diag : diags) {
            // Assuming your service returns a structured parsed object/map
            CodeTypes.ParsedCodeResult parsedCode = parseCode(diag);
            String codeType = parsedCode.codeType();
            String code = parsedCode.code();
            String system = getSystemForCodeType(codeType);
//            String codeDesc = codesService.lookupCodeDescription(diag);

            CodeDetails details = new CodeDetails(code, "", codeType, system);
            diagnosisMap.put(code, details);
        }

        return diagnosisMap;
    }

}
