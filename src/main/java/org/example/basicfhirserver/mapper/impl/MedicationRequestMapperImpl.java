package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.MedicationRequestMapper;
import org.example.basicfhirserver.mapper.utils.CodeTypes;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants.*;
import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;


@Component
public class MedicationRequestMapperImpl implements MedicationRequestMapper {

    @Override
    public MedicationRequest toR4(PrescriptionDBRecord prescriptionDBRecord) {

        MedicationRequest medicationRequest = new MedicationRequest();

        medicationRequest.setMeta(populateMeta(prescriptionDBRecord));
        medicationRequest.setText(populateNarrative());
        medicationRequest.setId(prescriptionDBRecord.getUuid().toString());
        medicationRequest.addNote(populateNote(prescriptionDBRecord));
        medicationRequest.setStatus(populateStatus(prescriptionDBRecord));
        medicationRequest.setIntent(populateIntent(prescriptionDBRecord));
        medicationRequest.setCategory(populateCategory(prescriptionDBRecord));

        populateReported(medicationRequest, prescriptionDBRecord);

        medicationRequest.setMedication(populateMedication(prescriptionDBRecord));
        medicationRequest.setSubject(populateSubject(prescriptionDBRecord));
        medicationRequest.setEncounter(populateEncounter(prescriptionDBRecord));

        populateAuthoredOn(medicationRequest, prescriptionDBRecord);
        medicationRequest.setRequester(populateRequestor(prescriptionDBRecord));

        populateReasonCodeAndReference(medicationRequest, prescriptionDBRecord);
        populateDosageInstruction(medicationRequest, prescriptionDBRecord);
        medicationRequest.setDispenseRequest(populateDispenseRequest(prescriptionDBRecord));
        populateMedicationAdherenceExtension(medicationRequest, prescriptionDBRecord);

        return medicationRequest;
    }


    private org.hl7.fhir.r4.model.Meta populateMeta(
            PrescriptionDBRecord prescriptionDBRecord) {

        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest");
        if (prescriptionDBRecord.getDateModified() != null) {
            meta.setLastUpdated(
                    Date.from(prescriptionDBRecord.getDateModified().atZone(ZoneId.systemDefault()).toInstant())
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
                        .setValue("Medication Request "));
    }


    private Annotation populateNote(PrescriptionDBRecord prescriptionDBRecord) {
        Annotation note = new Annotation();
        note.setText(prescriptionDBRecord.getNote());
        return note;
    }

    private MedicationRequest.MedicationRequestStatus populateStatus(
            PrescriptionDBRecord prescriptionDBRecord) {

        String status = prescriptionDBRecord.getStatus() != null ? prescriptionDBRecord.getStatus() : "unknown";

        try {
            return (MedicationRequest.MedicationRequestStatus.fromCode(status));
        } catch (Exception e) {
            return MedicationRequest.MedicationRequestStatus.UNKNOWN;
        }

    }

    private MedicationRequest.MedicationRequestIntent populateIntent(
            PrescriptionDBRecord prescriptionDBRecord) {
        String intentValue = prescriptionDBRecord.getIntent() != null ? prescriptionDBRecord.getIntent() : "plan";

        try {
            return MedicationRequest.MedicationRequestIntent.fromCode(intentValue.toLowerCase());
        } catch (Exception e) {
            return MedicationRequest.MedicationRequestIntent.PLAN;
        }

    }

    private List<CodeableConcept> populateCategory(PrescriptionDBRecord prescriptionDBRecord) {

        CodeableConcept categoryConcept = new CodeableConcept();
        Coding categoryCoding = new Coding();
        categoryCoding.setSystem(
                HL7_MEDICATION_REQUEST_CATEGORY
        );
        if (prescriptionDBRecord.getCategory() != null) {
            categoryCoding.setCode(prescriptionDBRecord.getCategory());

            String display = prescriptionDBRecord.getCategoryTitle() != null ? prescriptionDBRecord.getCategoryTitle() : "";
            categoryCoding.setDisplay(display);

            categoryConcept.addCoding(categoryCoding);
        } else {
            categoryCoding.setCode("community");
            categoryCoding.setDisplay("Home/Community");
            categoryConcept.addCoding(categoryCoding);
        }
        return List.of(categoryConcept);
    }

    private void populateReported(MedicationRequest medicationRequest,
                                  PrescriptionDBRecord prescriptionDBRecord) {

        if (prescriptionDBRecord.getReportingSourceType() != null && !prescriptionDBRecord.getReportingSourceType().isEmpty()) {
            String resourceType = null;
            switch (prescriptionDBRecord.getReportingSourceType()) {
                case "user":
                    resourceType = "Practitioner";
                    break;
                case "patient_data":
                    resourceType = "Patient";
                    break;
                case "facility":
                    resourceType = "Organization";
                    break;
            }

            if (resourceType != null && prescriptionDBRecord.getReportingSourceUuid() != null) {
                Reference reference = new Reference(resourceType + "/" + prescriptionDBRecord.getReportingSourceUuid());
                medicationRequest.setReported(reference);
            }
        } else {
            if (prescriptionDBRecord.getOrganizationUuid() == null) {
                String isPrimary = prescriptionDBRecord.getIsPrimaryRecord() != null ? prescriptionDBRecord.getIsPrimaryRecord() : "1";
                medicationRequest.setReported(new BooleanType("0".equals(isPrimary)));
            } else {
                Organization org = new Organization();
                Reference primaryBusinessEntity = new Reference();
                primaryBusinessEntity.setType(org.getNameElement().toString());
                primaryBusinessEntity.setReference(org.getNameElement() + "/" + prescriptionDBRecord.getOrganizationUuid());
                medicationRequest.setReported(primaryBusinessEntity);
            }
        }
    }

    private CodeableConcept populateMedication(
            PrescriptionDBRecord prescriptionDBRecord) {

        CodeableConcept medicationConcept = new CodeableConcept();

        if (prescriptionDBRecord.getRxnormDrugcode() != null && !prescriptionDBRecord.getRxnormDrugcode().isEmpty()) {
            Coding rxnormCoding = new Coding();
            rxnormCoding.setSystem(RXNORM);
            rxnormCoding.setCode(prescriptionDBRecord.getRxnormDrugcode());

            if (prescriptionDBRecord.getDrug() != null) {
                rxnormCoding.setDisplay(prescriptionDBRecord.getDrug());
            }
            medicationConcept.addCoding(rxnormCoding);

        } else if (prescriptionDBRecord.getDrug() != null && !prescriptionDBRecord.getDrug().isEmpty()) {
            medicationConcept.setText(prescriptionDBRecord.getDrug());
        }

        return medicationConcept;
    }

    private Reference populateSubject(
            PrescriptionDBRecord prescriptionDBRecord) {

        Reference subjectReference = new Reference();

        if (prescriptionDBRecord.getPuuid() != null && !prescriptionDBRecord.getPuuid().toString().isEmpty()) {
            subjectReference.setReference("Patient/" + prescriptionDBRecord.getPuuid());
        } else {
            Extension dataMissingExtension = new Extension("http://hl7.org");
            dataMissingExtension.setValue(getUnknownCodeableConcept());
            subjectReference.addExtension(dataMissingExtension);
        }

        return subjectReference;
    }

    private Reference populateEncounter(
            PrescriptionDBRecord prescriptionDBRecord) {
        Reference encounterRef = new Reference();
        if (prescriptionDBRecord.getEuuid() != null && !prescriptionDBRecord.getEuuid().toString().isEmpty()) {
            encounterRef.setReference("Encounter/" + prescriptionDBRecord.getEuuid());
        }
        return encounterRef;

    }

    private void populateAuthoredOn(MedicationRequest medicationRequest,
                                    PrescriptionDBRecord prescriptionDBRecord) {

        if (prescriptionDBRecord.getDateAdded() != null) {
            DateTimeType authoredOn = new DateTimeType(
                    Date.from(prescriptionDBRecord.getDateAdded().atZone(ZoneId.systemDefault()).toInstant())
            );
            medicationRequest.setAuthoredOnElement(authoredOn);
        }

    }

    private Reference populateRequestor(
            PrescriptionDBRecord prescriptionDBRecord) {

        Reference reference = new Reference();
        if (prescriptionDBRecord.getPruuid() != null && !prescriptionDBRecord.getPruuid().toString().isEmpty()) {
            reference.setReference("Practitioner/" + prescriptionDBRecord.getPruuid());
        } else if (prescriptionDBRecord.getOrganizationUuid() != null) {
            reference.setReference("Organization/" + prescriptionDBRecord.getOrganizationUuid());
        }
        return reference;
    }

    private void populateReasonCodeAndReference(MedicationRequest medicationRequest,
                                                PrescriptionDBRecord prescriptionDBRecord) {

        if (prescriptionDBRecord.getPuuid() == null || prescriptionDBRecord.getPuuid().toString().isEmpty()) {
            return;
        }

        if (prescriptionDBRecord.getDiagnosis() != null && !prescriptionDBRecord.getDiagnosis().isEmpty()) {
            List<CodeableConcept> parsedConcepts = CodeTypes.parseCodesIntoCodeableConcepts(prescriptionDBRecord.getDiagnosis());

            for (CodeableConcept concept : parsedConcepts) {
                concept.getCoding().forEach(coding -> coding.setSystem(SNOMED_CT));
                medicationRequest.addReasonCode(concept);
            }
        }

    }

    private void populateDosageInstruction(MedicationRequest medicationRequest,
                                           PrescriptionDBRecord prescriptionDBRecord) {

        boolean hasInstructions = prescriptionDBRecord.getDrugDosageInstructions() != null;
        boolean hasDosage = prescriptionDBRecord.getDosage() != null;
        boolean hasRoute = prescriptionDBRecord.getRoute() != null;

        if (!hasInstructions && !hasDosage && !hasRoute) {
            return;
        }

        Dosage dosage = new Dosage();

        String dosageInstructions = prescriptionDBRecord.getDrugDosageInstructions() != null ?
                prescriptionDBRecord.getDrugDosageInstructions() : prescriptionDBRecord.getDosage();

        if (dosageInstructions != null && !dosageInstructions.isEmpty()) {
            if (!dosageInstructions.matches("-?\\d+(\\.\\d+)?")) {
                dosage.setText(dosageInstructions);
            }
        }

        if (prescriptionDBRecord.getIntervalCodes() != null && !prescriptionDBRecord.getIntervalCodes().isEmpty()) {
            CodeableConcept intervalConcept = new CodeableConcept();
            Coding intervalCoding = new Coding();
            intervalCoding.setSystem(HL7_TIMING_ABBREVIATION);
            intervalCoding.setCode(prescriptionDBRecord.getIntervalCodes());
            intervalCoding.setDisplay(prescriptionDBRecord.getIntervalNotes());
            intervalConcept.addCoding(intervalCoding);

            String conceptText = prescriptionDBRecord.getIntervalNotes() != null ?
                    prescriptionDBRecord.getIntervalNotes() : prescriptionDBRecord.getIntervalTitle();
            intervalConcept.setText(conceptText);

            Timing timing = new Timing();
            timing.setCode(intervalConcept);
            dosage.setTiming(timing);

        } else if (prescriptionDBRecord.getIntervalNotes() != null && !prescriptionDBRecord.getIntervalNotes().isEmpty()) {
            CodeableConcept intervalConcept = new CodeableConcept();
            intervalConcept.setText(prescriptionDBRecord.getIntervalNotes());

            Timing timing = new Timing();
            timing.setCode(intervalConcept);
            dosage.setTiming(timing);
        }

        if (prescriptionDBRecord.getPrescriptionDrugSize() != null && !prescriptionDBRecord.getPrescriptionDrugSize().isEmpty()) {
            String rawSize = prescriptionDBRecord.getPrescriptionDrugSize();
            if (rawSize.matches("-?\\d+(\\.\\d+)?")) {
                double quantityVal = Double.parseDouble(rawSize);

                Quantity doseQuantity = new Quantity();
                doseQuantity.setValue(quantityVal);

                String unitText = prescriptionDBRecord.getUnitTitle() != null ? prescriptionDBRecord.getUnitTitle() : "";
                doseQuantity.setUnit(unitText);

                Dosage.DosageDoseAndRateComponent doseAndRate = new Dosage.DosageDoseAndRateComponent();
                doseAndRate.setDose(doseQuantity);
                dosage.addDoseAndRate(doseAndRate);
            }
        }

        boolean hasRouteCodes = prescriptionDBRecord.getRouteCodes() != null && !prescriptionDBRecord.getRouteCodes().isEmpty();
        boolean hasRouteTitle = prescriptionDBRecord.getRouteTitle() != null && !prescriptionDBRecord.getRouteTitle().isEmpty();

        if (hasRouteCodes || hasRouteTitle) {
            CodeableConcept routeConcept = new CodeableConcept();

            if (hasRouteCodes) {
                List<CodeableConcept> routes = CodeTypes.parseCodesIntoCodeableConcepts(prescriptionDBRecord.getRouteCodes());
                if (!routes.isEmpty()) {
                    routeConcept = routes.get(0);
                }
            }

            if (hasRouteTitle) {
                routeConcept.setText(prescriptionDBRecord.getRouteTitle());
            }

            dosage.setRoute(routeConcept);
        }

        medicationRequest.addDosageInstruction(dosage);
    }

    private MedicationRequest.MedicationRequestDispenseRequestComponent populateDispenseRequest(
            PrescriptionDBRecord prescriptionDBRecord) {

        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest =
                new MedicationRequest.MedicationRequestDispenseRequestComponent();

        int refills = 0;
        dispenseRequest.setNumberOfRepeatsAllowed(refills);

        if (prescriptionDBRecord.getQuantity() != null && !prescriptionDBRecord.getQuantity().isEmpty()) {
            String rawQuantity = prescriptionDBRecord.getQuantity();
            if (rawQuantity.matches("-?\\d+(\\.\\d+)?")) {
                int quantityVal = (int) Double.parseDouble(rawQuantity);

                Quantity quantityElement = new Quantity();
                quantityElement.setValue(quantityVal);

                String unitText = prescriptionDBRecord.getUnitTitle() != null ? prescriptionDBRecord.getUnitTitle() : "";
                quantityElement.setUnit(unitText);

                dispenseRequest.setQuantity(quantityElement);
            }
        }

        return dispenseRequest;
    }

    private void populateMedicationAdherenceExtension(MedicationRequest medicationRequest,
                                                      PrescriptionDBRecord prescriptionDBRecord) {

        if (prescriptionDBRecord.getMedicationAdherence() == null || prescriptionDBRecord.getMedicationAdherence().isEmpty()
                || prescriptionDBRecord.getMedicationAdherenceDateAsserted() == null
                || prescriptionDBRecord.getMedicationAdherenceInformationSource() == null || prescriptionDBRecord.getMedicationAdherenceInformationSource().isEmpty()) {
            return;
        }

        Extension mainExtension = new Extension();
        mainExtension.setUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-medication-adherence");

        Extension adherenceExtension = new Extension();
        adherenceExtension.setUrl("medicationAdherence");

        if (prescriptionDBRecord.getMedicationAdherence() != null && !prescriptionDBRecord.getMedicationAdherence().isEmpty()
                && prescriptionDBRecord.getMedicationAdherenceTitle() != null && !prescriptionDBRecord.getMedicationAdherenceTitle().isEmpty()) {

            CodeTypes.ParsedCodeResult parsedCode = CodeTypes.parseCode(prescriptionDBRecord.getMedicationAdherenceCodes());

            CodeableConcept concept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(parsedCode.code());
            coding.setSystem(CodeTypes.getSystemForCodeType(parsedCode.codeType()));
            coding.setDisplay(prescriptionDBRecord.getMedicationAdherenceTitle());
            concept.addCoding(coding);
            adherenceExtension.setValue(concept);

        } else {
            adherenceExtension.setValue(getUnknownCodeableConcept());
        }
        mainExtension.addExtension(adherenceExtension);

        Extension dateExtension = new Extension();
        dateExtension.setUrl("dateAsserted");

        DateTimeType formattedDate = new DateTimeType(
                Date.from(prescriptionDBRecord.getMedicationAdherenceDateAsserted().atZone(ZoneId.systemDefault()).toInstant())
        );
        dateExtension.setValue(formattedDate);
        mainExtension.addExtension(dateExtension);

        Extension sourceExtension = new Extension();
        sourceExtension.setUrl("informationSource");

        if (prescriptionDBRecord.getMedicationAdherenceInformationSource() != null && !prescriptionDBRecord.getMedicationAdherenceInformationSource().isEmpty()
                && prescriptionDBRecord.getMedicationAdherenceInformationSourceTitle() != null && !prescriptionDBRecord.getMedicationAdherenceInformationSourceTitle().isEmpty()) {

            CodeTypes.ParsedCodeResult parsedCode = CodeTypes.parseCode(prescriptionDBRecord.getMedicationAdherenceInformationSource());

            CodeableConcept concept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(parsedCode.code());
            coding.setSystem(CodeTypes.getSystemForCodeType(parsedCode.codeType()));
            coding.setDisplay(prescriptionDBRecord.getMedicationAdherenceInformationSourceTitle());
            concept.addCoding(coding);
            sourceExtension.setValue(concept);
        } else {
            CodeableConcept unknownConcept = getUnknownCodeableConcept();
            sourceExtension.setValue(unknownConcept);
        }
        mainExtension.addExtension(sourceExtension);
        medicationRequest.addExtension(mainExtension);
    }
}
