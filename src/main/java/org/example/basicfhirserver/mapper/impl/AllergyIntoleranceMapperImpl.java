package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.AllergyIntoleranceMapper;
import org.example.basicfhirserver.mapper.utils.CodeTypes;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;

@Component
public class AllergyIntoleranceMapperImpl implements AllergyIntoleranceMapper {
    @Override
    public AllergyIntolerance toR4(AllergyDBRecord allergyDBRecord) {
        AllergyIntolerance allergyIntolerance = new AllergyIntolerance();

        allergyIntolerance.setMeta(populateMeta(allergyDBRecord));
        allergyIntolerance.setText(populateNarrative(allergyDBRecord));
        allergyIntolerance.setId(allergyDBRecord.getUuid() != null ? allergyDBRecord.getUuid().toString() :
                allergyDBRecord.getId().toString());

        allergyIntolerance.setClinicalStatus(populateClinicalStatus(allergyDBRecord));
        allergyIntolerance.setCategory(populateCategory());

        populateCriticality(allergyIntolerance, allergyDBRecord);
        allergyIntolerance.setPatient(populatePatient(allergyDBRecord));
        allergyIntolerance.setRecorder(populateRecorder(allergyDBRecord));
        allergyIntolerance.setReaction(populateReaction(allergyDBRecord));
        allergyIntolerance.setCode(populateCode(allergyDBRecord));
        allergyIntolerance.setVerificationStatus(populateVerification(allergyDBRecord));

        return allergyIntolerance;


    }

    private org.hl7.fhir.r4.model.Meta populateMeta(
            AllergyDBRecord allergyDBRecord) {

        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-allergyintolerance");
        if (allergyDBRecord.getModifydate() != null) {
            meta.setLastUpdated(
                    Date.from(allergyDBRecord.getModifydate().atZone(ZoneId.systemDefault()).toInstant())
            );
            return meta;
        }
        meta.setLastUpdated(
                Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
        );
        return meta;
    }

    private Narrative populateNarrative(AllergyDBRecord allergyDBRecord) {
        return new Narrative()
                .setStatus(Narrative.NarrativeStatus.ADDITIONAL)
                .setDiv(new XhtmlNode(NodeType.Element, "div")
                        .setValue(allergyDBRecord.getTitle()));
    }

    private CodeableConcept populateClinicalStatus(AllergyDBRecord allergyDBRecord) {

        String clinicalStatus = "inactive";
        if (allergyDBRecord.getOutcome() == '1' && allergyDBRecord.getEnddate() != null) {
            clinicalStatus = "resolved";
        } else if (allergyDBRecord.getEnddate() == null) {
            clinicalStatus = "active";
        }

        CodeableConcept clinicalStatusConcept = new CodeableConcept();
        Coding code = new Coding();
        code.setSystem("http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical");
        code.setCode(clinicalStatus);
        code.setDisplay(clinicalStatus.toUpperCase());

        clinicalStatusConcept.addCoding(code);
        return clinicalStatusConcept;
    }

    private List<Enumeration<AllergyIntolerance.AllergyIntoleranceCategory>> populateCategory() {
        return List.of(
                new Enumeration<>(new AllergyIntolerance.AllergyIntoleranceCategoryEnumFactory(), AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION)
        );
    }

    private void populateCriticality(AllergyIntolerance allergyIntolerance, AllergyDBRecord allergyDBRecord) {
        String severity = allergyDBRecord.getSeverityAl();
        if (severity != null) {
            RiskPair risk = CRITICALITY_CODE.get(severity);

            if (risk != null) {
                allergyIntolerance.setCriticality(risk.code());
            } else {
                allergyIntolerance.setCriticality(AllergyIntolerance.AllergyIntoleranceCriticality.NULL);
            }
        }
    }


    private Reference populatePatient(
            AllergyDBRecord allergyDBRecord) {

        Reference subjectReference = new Reference();

        if (allergyDBRecord.getPuuid() != null && !allergyDBRecord.getPuuid().toString().isEmpty()) {
            subjectReference.setReference("Patient/" + allergyDBRecord.getPuuid());
        } else {
            Extension dataMissingExtension = new Extension("http://hl7.org");
            dataMissingExtension.setValue(getUnknownCodeableConcept());
            subjectReference.addExtension(dataMissingExtension);
        }

        return subjectReference;
    }

    private Reference populateRecorder(
            AllergyDBRecord allergyDBRecord) {
        Reference subjectReference = new Reference();

        if (allergyDBRecord.getPractitionerUuid() != null && !allergyDBRecord.getPractitionerUuid().toString().isEmpty()) {
            subjectReference.setReference("Practitioner/" + allergyDBRecord.getPractitionerUuid());
        } else {
            Extension dataMissingExtension = new Extension("http://hl7.org");
            dataMissingExtension.setValue(getUnknownCodeableConcept());
            subjectReference.addExtension(dataMissingExtension);
        }

        return subjectReference;
    }

    private List<AllergyIntolerance.AllergyIntoleranceReactionComponent> populateReaction(
            AllergyDBRecord allergyDBRecord
    ) {

        AllergyIntolerance.AllergyIntoleranceReactionComponent reactions = new AllergyIntolerance.AllergyIntoleranceReactionComponent();

        String reaction = allergyDBRecord.getReaction();
        if (reaction != null && !reaction.equals("unassigned") &&
                allergyDBRecord.getReactionCodes() != null
                && !allergyDBRecord.getReactionCodes().isEmpty()) {

            CodeableConcept reactionConcept = new CodeableConcept();

            String[] reactionCodes = allergyDBRecord.getReactionCodes().split(":");
            for (String rawCode : reactionCodes) {
                CodeTypes.ParsedCodeResult parsedCode = CodeTypes.parseCode(rawCode);
                String codeType = parsedCode.codeType();
                String code = parsedCode.code();
                String system = CodeTypes.getSystemForCodeType(codeType);

                Coding coding = new Coding();
                coding.setCode(code);
                coding.setDisplay(allergyDBRecord.getReactionTitle());
                coding.setSystem(system);
                reactionConcept.addCoding(coding);
            }

            reactions.addManifestation(reactionConcept);

        } else {
            reactions.addManifestation(getUnknownCodeableConcept());
        }
        return List.of(reactions);
    }

    private CodeableConcept populateCode(
            AllergyDBRecord allergyDBRecord
    ) {
        String diagnosis = allergyDBRecord.getDiagnosis();
        if (diagnosis != null && !diagnosis.isEmpty()) {

            CodeableConcept diagnosisCode = new CodeableConcept();

            String[] diagnosisCodes = diagnosis.split(":");

            for (String rawCode : diagnosisCodes) {
                CodeTypes.ParsedCodeResult parsedCode = CodeTypes.parseCode(rawCode);
                String codeType = parsedCode.codeType();
                String code = parsedCode.code();
                String system = CodeTypes.getSystemForCodeType(codeType);

                Coding coding = new Coding();
                coding.setCode(code);
                coding.setDisplay(allergyDBRecord.getReactionTitle());
                coding.setSystem(system);
                diagnosisCode.addCoding(coding);
            }
            return diagnosisCode;

        } else {
            return getUnknownCodeableConcept();
        }

    }

    private CodeableConcept populateVerification(
            AllergyDBRecord allergyDBRecord
    ) {
        CodeableConcept verificationStatus = new CodeableConcept();
        if (allergyDBRecord.getVerification() != null && !allergyDBRecord.getVerification().isEmpty()) {
            Coding coding = new Coding();
            coding.setCode(allergyDBRecord.getVerification());
            coding.setDisplay(allergyDBRecord.getVerificationTitle());
            coding.setSystem("http://terminology.hl7.org/CodeSystem/allergyintolerance-verification");
            verificationStatus.addCoding(coding);
        } else {
            Coding coding = new Coding();
            coding.setCode("unconfirmed");
            coding.setDisplay("unconfirmed");
            coding.setSystem("http://terminology.hl7.org/CodeSystem/allergyintolerance-verification");
            verificationStatus.addCoding(coding);
        }
        return verificationStatus;
    }


    private record RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality code, String display) {
    }

    private static final Map<String, RiskPair> CRITICALITY_CODE = Map.ofEntries(
            Map.entry("mild", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.LOW, "Low Risk")),
            Map.entry("mild_to_moderate", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.LOW, "Low Risk")),
            Map.entry("moderate", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.LOW, "Low Risk")),
            Map.entry("moderate_to_severe", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH, "High Risk")),
            Map.entry("severe", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH, "High Risk")),
            Map.entry("life_threatening_severity", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH, "High Risk")),
            Map.entry("fatal", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.HIGH, "High Risk")),
            // Fixed: Mapping unassigned to UNABLETOASSESS instead of HIGH
            Map.entry("unassigned", new RiskPair(AllergyIntolerance.AllergyIntoleranceCriticality.UNABLETOASSESS, "Unable to Assess Risk"))
    );

}
