package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.mapper.utils.CodeTypes;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.service.assembler.condition.ConditionAssembler;
import org.hl7.fhir.r4.model.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;

@Component
public class ConditionMapperImpl implements ConditionMapper {

    private static final String US_CORE_CONDITION_ENCOUNTER_DIAGNOSIS_PROFILE =
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition-encounter-diagnosis";
    private static final String US_CORE_CONDITION_PROFILE =
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition";
    private static final String US_CORE_CONDITION_PROBLEMS_HEALTH_CONCERNS_PROFILE =
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-condition-problems-health-concerns";
    private static final String HL7_CONDITION_CLINICAL_SYSTEM =
            "http://terminology.hl7.org/CodeSystem/condition-clinical";
    private static final String HL7_CONDITION_ASSERTED_DATE_EXTENSION =
            "http://hl7.org/fhir/StructureDefinition/condition-assertedDate";

    @Override
    public Condition toR4(ConditionCanonical conditionCanonical) {

        Condition condition = new Condition();
        condition.setMeta(populateMeta(conditionCanonical));
        condition.setId(conditionCanonical.getId());

        condition.setCategory(populateCategory(conditionCanonical));
        condition.setCode(populateCode(conditionCanonical));
        condition.setSubject(populateSubject(conditionCanonical));
        if (Objects.equals(conditionCanonical.getCategory(), ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS)) {
            condition.setEncounter(populateEncounter(conditionCanonical));
        }

        condition.setClinicalStatus(populateClinicalStatus(conditionCanonical));
        condition.setVerificationStatus(populateVerificationStatus(conditionCanonical));
        condition.setRecordedDate(populateRecordedDate(conditionCanonical));

        if (conditionCanonical.getOnsetDate() != null) {
            condition.addExtension(populateAssertedDate(conditionCanonical));
        }

        if (Objects.equals(conditionCanonical.getCategory(), "encounter-diagnosis")) {
            condition.setRecorder(populateRecorder(conditionCanonical));
        }

        if (conditionCanonical.getBegDate() != null) {
            condition.setOnset(populateOnsetDateTime(conditionCanonical));
        }

        if (conditionCanonical.getEndDate() != null) {
            condition.setOnset(populateAbatementDateTime(conditionCanonical));
        }

        if (conditionCanonical.getComments() != null) {
            condition.setNote(populateNote(conditionCanonical));
        }

        return condition;
    }


    private org.hl7.fhir.r4.model.Meta populateMeta(
            ConditionCanonical conditionCanonical) {
        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");

        if (Objects.equals(conditionCanonical.getCategory(), "encounter-diagnosis")) {
            meta.addProfile(US_CORE_CONDITION_ENCOUNTER_DIAGNOSIS_PROFILE);
        } else {
            CanonicalType profile1 = new CanonicalType();
            profile1.setValue(US_CORE_CONDITION_PROFILE);

            CanonicalType profile2 = new CanonicalType();
            profile2.setValue(US_CORE_CONDITION_PROBLEMS_HEALTH_CONCERNS_PROFILE);

            meta.setProfile(List.of(profile1, profile2));
        }

        if (conditionCanonical.getLastUpdated() != null) {
            meta.setLastUpdated(
                    Date.from(conditionCanonical.getLastUpdated().atZone(ZoneId.systemDefault()).toInstant())
            );
            return meta;
        }
        meta.setLastUpdated(
                Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
        );
        return meta;
    }

    private List<CodeableConcept> populateCategory(ConditionCanonical conditionCanonical) {
        CodeableConcept codeableConcept = new CodeableConcept();
        if (Objects.equals(conditionCanonical.getCategory(), ConditionAssembler.CATEGORY_PROBLEM_LIST)) {
            Coding coding = new Coding();
            coding.setSystem(FhirCodeSystemConstants.HL7_CONDITION_CATEGORY);
            coding.setCode(ConditionAssembler.CATEGORY_PROBLEM_LIST);
            coding.setDisplay("Problem List Item");
            codeableConcept.setText("Problem List Item");
            codeableConcept.addCoding(coding);

        } else if (Objects.equals(conditionCanonical.getCategory(), ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS)) {
            Coding coding = new Coding();
            coding.setSystem(FhirCodeSystemConstants.HL7_CONDITION_CATEGORY);
            coding.setCode(ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS);
            coding.setDisplay("Encounter Diagnosis");
            codeableConcept.setText("Encounter Diagnosis");
            codeableConcept.addCoding(coding);

        } else if (Objects.equals(conditionCanonical.getCategory(), ConditionAssembler.CATEGORY_HEALTH_CONCERNS)) {
            Coding coding = new Coding();
            coding.setSystem(FhirCodeSystemConstants.HL7_CONDITION_CATEGORY_3_1_1);
            coding.setCode(ConditionAssembler.CATEGORY_HEALTH_CONCERNS);
            coding.setDisplay("Health Concern");
            codeableConcept.setText("Health Concern");
            codeableConcept.addCoding(coding);

            if (conditionCanonical.getHealthConcernSubtype() != null &&
                    conditionCanonical.getHealthConcernSubtypeTitle() != null) {
                Coding healthConcern = new Coding();
                coding.setSystem(getSystem(conditionCanonical));
                coding.setCode(conditionCanonical.getHealthConcernSubtype());
                coding.setDisplay(conditionCanonical.getHealthConcernSubtypeTitle());
                codeableConcept.setText(conditionCanonical.getHealthConcernSubtypeTitle());
                codeableConcept.addCoding(healthConcern);
            }
        }
        return List.of(codeableConcept);
    }

    private @NonNull String getSystem(ConditionCanonical conditionCanonical) {
        String system = FhirCodeSystemConstants.HL7_US_CORE_CATEGORY_OBSERVATION;

        List<String> US_CORE_CODESYSTEM_OBSERVATION_CATEGORY = List.of(
                "social-history", "vital-signs", "imaging", "laboratory",
                "procedure", "survey", "exam", "therapy", "activity"
        );

        if (US_CORE_CODESYSTEM_OBSERVATION_CATEGORY.contains(conditionCanonical.getHealthConcernSubtype())) {
            system = FhirCodeSystemConstants.HL7_CATEGORY_OBSERVATION;
        }
        return system;
    }

    private CodeableConcept populateCode(ConditionCanonical conditionCanonical) {

        String diagnosis = conditionCanonical.getDiagnosis();
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
//                coding.setDisplay();
                coding.setSystem(system);
                diagnosisCode.addCoding(coding);
            }
            return diagnosisCode;

        } else {
            return getUnknownCodeableConcept();
        }
    }

    private Reference populateSubject(
            ConditionCanonical conditionCanonical) {
        Reference subjectReference = new Reference();

        if (conditionCanonical.getPatientUuid() != null) {
            subjectReference.setReference("Patient/" + conditionCanonical.getPatientUuid());
        }
        return subjectReference;
    }

    private Reference populateEncounter(
            ConditionCanonical conditionCanonical) {
        Reference encounterReference = new Reference();

        if (conditionCanonical.getEncounterUuid() != null) {
            encounterReference.setReference("Encounter/" + conditionCanonical.getEncounterUuid());
        }
        return encounterReference;
    }

    private CodeableConcept populateClinicalStatus(ConditionCanonical conditionCanonical) {

        CodeableConcept clinicalStatus = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(conditionCanonical.getClinicalStatus());
        coding.setSystem(HL7_CONDITION_CLINICAL_SYSTEM);
        coding.setDisplay(
                conditionCanonical.getClinicalStatus().isEmpty() ? "" :
                        conditionCanonical.getClinicalStatus().substring(0, 1).toUpperCase(Locale.ROOT) +
                                conditionCanonical.getClinicalStatus().substring(1)
        );
        clinicalStatus.addCoding(coding);

        return clinicalStatus;
    }

    private CodeableConcept populateVerificationStatus(ConditionCanonical conditionCanonical) {

        CodeableConcept clinicalStatus = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(conditionCanonical.getVerificationStatus());
        coding.setSystem(HL7_CONDITION_CLINICAL_SYSTEM);
        String raw = conditionCanonical.getVerificationStatus().replace("-", " ");
        String clean = raw.isEmpty() ? "" : raw.substring(0, 1).toUpperCase(Locale.ROOT) + raw.substring(1);
        coding.setDisplay(clean);

        clinicalStatus.addCoding(coding);

        return clinicalStatus;
    }

    private Date populateRecordedDate(ConditionCanonical conditionCanonical) {
        LocalDateTime recordedDate = conditionCanonical.getOnsetDate() != null ? conditionCanonical.getOnsetDate() :
                conditionCanonical.getBegDate();

        return Date.from(recordedDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Extension populateAssertedDate(ConditionCanonical conditionCanonical) {
        LocalDateTime date = conditionCanonical.getOnsetDate();

        Extension extension = new Extension();
        extension.setUrl(HL7_CONDITION_ASSERTED_DATE_EXTENSION);
        extension.setValue(new DateTimeType(date.toString()));

        return extension;
    }

    private Reference populateRecorder(ConditionCanonical conditionCanonical) {
        Reference recorderReference = new Reference();

        if (conditionCanonical.getCreatorUuid() != null && conditionCanonical.getCreatorNpi() != null) {
            recorderReference.setReference("Practitioner/" + conditionCanonical.getCreatorUuid());
        }
        return recorderReference;
    }

    private DateTimeType populateOnsetDateTime(ConditionCanonical conditionCanonical) {
        LocalDateTime begDate = conditionCanonical.getBegDate();

        Instant utcInstant = begDate.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();
        Date utcDate = Date.from(utcInstant);
        return new DateTimeType(utcDate);
    }

    private DateTimeType populateAbatementDateTime(ConditionCanonical conditionCanonical) {
        LocalDateTime endDate = conditionCanonical.getEndDate();

        Instant utcInstant = endDate.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toInstant();
        Date utcDate = Date.from(utcInstant);
        return new DateTimeType(utcDate);
    }

    private List<Annotation> populateNote(ConditionCanonical conditionCanonical) {
        Annotation note = new Annotation();
        note.setText(conditionCanonical.getComments());
        return List.of(note);

    }

}

