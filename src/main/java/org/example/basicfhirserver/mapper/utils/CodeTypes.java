package org.example.basicfhirserver.mapper.utils;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import java.util.ArrayList;
import java.util.List;

public class CodeTypes {

    public static final String CODE_TYPE_SNOMED_CT = "SNOMED-CT";
    public static final String CODE_TYPE_SNOMED = "SNOMED";
    public static final String CODE_TYPE_CPT4 = "CPT4";
    public static final String CODE_TYPE_CPT = "CPT";
    public static final String CODE_TYPE_LOINC = "LOINC";
    public static final String CODE_TYPE_ICD10 = "ICD10";
    public static final String CODE_TYPE_RXCUI = "RXCUI";
    public static final String CODE_TYPE_RXNORM = "RXNORM";
    public static final String CODE_TYPE_CVX = "CVX";
    public static final String CODE_TYPE_ICD10PCS = "ICD10PCS";
    public static final String CODE_TYPE_NUCC = "NUCC";
    public static final String CODE_TYPE_NDC = "NDC";
    public static final String CODE_TYPE_NCI = "NCI";
    public static final String CODE_TYPE_DATE_ABSENT_REASON = "DATE_ABSENT_REASON";
    public static final String CODE_TYPE_HL7_ROLE_CODE = "HL7_ROLE_CODE";
    public static final String CODE_TYPE_HL7_PARTICIPATION_FUNCTION = "HL7_PARTICIPATION_FUNCTION";
    public static final String CODE_TYPE_HSOC = "HSOC";

    public record ParsedCodeResult(String code, String codeType) {
    }

    public static ParsedCodeResult parseCode(String code) {
        if (code != null && code.contains(":")) {
            String[] parts = code.split(":", 2);
            return new ParsedCodeResult(parts[1], parts[0]);
        }

        return new ParsedCodeResult(code, null);
    }


    public static String getSystemForCodeType(String codeType, boolean useOid) {
        String system = null;

        if (useOid) {
            if (CODE_TYPE_SNOMED_CT.equals(codeType) || CODE_TYPE_SNOMED.equals(codeType)) {
                system = "2.16.840.1.113883.6.96";
            } else if (CODE_TYPE_CPT4.equals(codeType) || CODE_TYPE_CPT.equals(codeType)) {
                system = "2.16.840.1.113883.6.12";
            } else if (CODE_TYPE_LOINC.equals(codeType)) {
                system = "2.16.840.1.113883.6.1";
            } else if (CODE_TYPE_ICD10.equals(codeType)) {
                system = "2.16.840.1.113883.6.90";
            } else if (CODE_TYPE_RXCUI.equals(codeType) || CODE_TYPE_RXNORM.equals(codeType)) {
                system = "2.16.840.1.113883.6.88";
            } else if (CODE_TYPE_CVX.equals(codeType)) {
                system = "2.16.840.1.113883.12.292";
            } else if (CODE_TYPE_ICD10PCS.equals(codeType)) {
                system = "2.16.840.1.113883.6.4";
            }
        } else {
            if (codeType == null) {
                return null;
            }
            switch (codeType) {
                case CODE_TYPE_SNOMED_CT:
                case CODE_TYPE_SNOMED:
                    system = "http://snomed.info/sct";
                    break;
                case CODE_TYPE_NUCC:
                    system = "http://nucc.org/provider-taxonomy";
                    break;
                case CODE_TYPE_LOINC:
                    system = "http://loinc.org";
                    break;
                case CODE_TYPE_RXNORM:
                case CODE_TYPE_RXCUI:
                    system = "http://www.nlm.nih.gov/research/umls/rxnorm";
                    break;
                case CODE_TYPE_NDC:
                    system = "http://hl7.org/fhir/sid/ndc";
                    break;
                case CODE_TYPE_NCI:
                    system = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl";
                    break;
                case CODE_TYPE_CPT4:
                case CODE_TYPE_CPT:
                    system = "http://www.ama-assn.org/go/cpt";
                    break;
                case CODE_TYPE_ICD10:
                    system = "http://hl7.org/fhir/sid/icd-10";
                    break;
                case CODE_TYPE_DATE_ABSENT_REASON:
                    system = "http://terminology.hl7.org/CodeSystem/data-absent-reason";
                    break;
                case CODE_TYPE_HL7_ROLE_CODE:
                    system = "http://terminology.hl7.org/CodeSystem/role-code";
                    break;
                case CODE_TYPE_HL7_PARTICIPATION_FUNCTION:
                    system = "http://terminology.hl7.org/CodeSystem/v3-ParticipationType";
                    break;
                case CODE_TYPE_HSOC:
                    system = "https://www.cdc.gov/nhsn/cdaportal/terminology/codesystem/hsloc.html";
                    break;
                default:
                    break;
            }
        }


        // Dynamic fallback logic matching the PHP CODE_TYPE_OID array loop lookup
//        if (system == null || system.isEmpty()) {
//            system = OpenEmrCodeOidRegistry.getOidByCodeType(codeType);
//        }

        return system;
    }

    public static String getSystemForCodeType(String codeType) {
        return getSystemForCodeType(codeType, false);
    }

    public static List<CodeableConcept> parseCodesIntoCodeableConcepts(String codes) {
        List<CodeableConcept> codeableConcepts = new ArrayList<>();

        if (codes == null || codes.trim().isEmpty()) {
            return codeableConcepts;
        }

        String[] codeItems = codes.split(";");

        for (String codeItem : codeItems) {
            if (codeItem.trim().isEmpty()) continue;

            CodeTypes.ParsedCodeResult parsedCode = CodeTypes.parseCode(codeItem.trim());
            String codeType = parsedCode.codeType();
            String code = parsedCode.code();
            String system = CodeTypes.getSystemForCodeType(codeType);
//          String codeDesc = CodeTypes.lookupCodeDescription(codeItem.trim());

            // Build the standard HAPI FHIR structural model directly
            CodeableConcept concept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setCode(code);
            coding.setSystem(system);
//          coding.setDisplay(codeDesc);

            concept.addCoding(coding);
//            concept.setText(codeDesc);
            codeableConcepts.add(concept);
        }

        return codeableConcepts;
    }

//    public String lookupCodeDescription(String codes, String descDetail) {
//        if (codes == null || codes.isEmpty()) {
//            return "";
//        }
//
//        // Invokes your legacy OpenEMR native table descriptor lookup method
//        String codeText = legacyLookupCodeDescriptions(codes, descDetail);
//
//        if (codeText == null || codeText.isEmpty()) {
//            String fallbackDetail = "code_text".equals(descDetail) ? "code_text_short" : "code_text";
//            codeText = legacyLookupCodeDescriptions(codes, fallbackDetail);
//        }
//
//        return codeText != null ? codeText : "";
//    }
//
//    public String lookupCodeDescription(String codes) {
//        return lookupCodeDescription(codes, "code_text");
//    }


}
