package org.example.basicfhirserver.mapper.utils;

import lombok.Getter;

@Getter
public enum PlaceOfService {
  PHARMACY("01", "Pharmacy"),
  TELEHEALTH("02", "Telehealth"),
  SCHOOL("03", "School"),
  HOMELESS_SHELTER("04", "Homeless Shelter"),
  IHS_FREESTANDING("05", "Indian Health Service Free-standing Facility"),
  IHS_PROVIDER_BASED("06", "Indian Health Service Provider-based Facility"),
  TRIBAL_638_FREESTANDING("07", "Tribal 638 Free-standing Facility"),
  TRIBAL_638_PROVIDER_BASED("08", "Tribal 638 Provider-based Facility"),
  PRISON_CORRECTIONAL("09", "Prison Correctional Facility"),
  UNASSIGNED_10("10", "Unassigned"),
  OFFICE("11", "Office"),
  HOME("12", "Home"),
  ASSISTED_LIVING("13", "Assisted Living Facility"),
  GROUP_HOME("14", "Group Home *"),
  MOBILE_UNIT("15", "Mobile Unit"),
  TEMPORARY_LODGING("16", "Temporary Lodging"),
  WALKIN_RETAIL_HEALTH("17", "Walk-in Retail Health Clinic"),
  PLACE_OF_EMPLOYMENT("18", "Place of Employment-Worksite"),
  OFF_CAMPUS_OUTPATIENT("19", "Off Campus-Outpatient Hospital"),
  URGENT_CARE("20", "Urgent Care Facility"),
  INPATIENT_HOSPITAL("21", "Inpatient Hospital"),
  OUTPATIENT_HOSPITAL("22", "Outpatient Hospital"),
  EMERGENCY_ROOM("23", "Emergency Room - Hospital"),
  AMBULATORY_SURGICAL("24", "Ambulatory Surgical Center"),
  BIRTHING_CENTER("25", "Birthing Center"),
  MILITARY_TREATMENT("26", "Military Treatment Facility"),
  UNASSIGNED_27("27", "Unassigned"),
  UNASSIGNED_28("28", "Unassigned"),
  UNASSIGNED_29("29", "Unassigned"),
  UNASSIGNED_30("30", "Unassigned"),
  SKILLED_NURSING("31", "Skilled Nursing Facility"),
  NURSING_FACILITY("32", "Nursing Facility"),
  CUSTODIAL_CARE("33", "Custodial Care Facility"),
  HOSPICE("34", "Hospice"),
  UNASSIGNED_35("35", "Unassigned"),
  UNASSIGNED_36("36", "Unassigned"),
  UNASSIGNED_37("37", "Unassigned"),
  UNASSIGNED_38("38", "Unassigned"),
  UNASSIGNED_39("39", "Unassigned"),
  UNASSIGNED_40("40", "Unassigned"),
  AMBULANCE_LAND("41", "Ambulance - Land"),
  AMBULANCE_AIR_WATER("42", "Ambulance - Air or Water"),
  UNASSIGNED_43("43", "Unassigned"),
  UNASSIGNED_44("44", "Unassigned"),
  UNASSIGNED_45("45", "Unassigned"),
  UNASSIGNED_46("46", "Unassigned"),
  UNASSIGNED_47("47", "Unassigned"),
  UNASSIGNED_48("48", "Unassigned"),
  INDEPENDENT_CLINIC("49", "Independent Clinic"),
  FEDERALLY_QUALIFIED_HEALTH("50", "Federally Qualified Health Center"),
  INPATIENT_PSYCHIATRIC("51", "Inpatient Psychiatric Facility"),
  PSYCHIATRIC_PARTIAL_HOSPITALIZATION("52", "Psychiatric Facility-Partial Hospitalization"),
  COMMUNITY_MENTAL_HEALTH("53", "Community Mental Health Center"),
  INTERMEDIATE_CARE_MENTALLY_RETARDED("54", "Intermediate Care Facility/Mentally Retarded"),
  RESIDENTIAL_SUBSTANCE_ABUSE("55", "Residential Substance Abuse Treatment Facility"),
  PSYCHIATRIC_RESIDENTIAL("56", "Psychiatric Residential Treatment Center"),
  NONRESIDENTIAL_SUBSTANCE_ABUSE("57", "Non-residential Substance Abuse Treatment Facility"),
  UNASSIGNED_58("58", "Unassigned"),
  UNASSIGNED_59("59", "Unassigned"),
  MASS_IMMUNIZATION("60", "Mass Immunization Center"),
  COMPREHENSIVE_INPATIENT_REHAB("61", "Comprehensive Inpatient Rehabilitation Facility"),
  COMPREHENSIVE_OUTPATIENT_REHAB("62", "Comprehensive Outpatient Rehabilitation Facility"),
  UNASSIGNED_63("63", "Unassigned"),
  UNASSIGNED_64("64", "Unassigned"),
  END_STAGE_RENAL_DISEASE("65", "End-Stage Renal Disease Treatment Facility"),
  UNASSIGNED_66("66", "Unassigned"),
  UNASSIGNED_67("67", "Unassigned"),
  UNASSIGNED_68("68", "Unassigned"),
  UNASSIGNED_69("69", "Unassigned"),
  UNASSIGNED_70("70", "Unassigned"),
  PUBLIC_HEALTH_CLINIC("71", "Public Health Clinic"),
  RURAL_HEALTH_CLINIC("72", "Rural Health Clinic"),
  UNASSIGNED_73("73", "Unassigned"),
  UNASSIGNED_74("74", "Unassigned"),
  UNASSIGNED_75("75", "Unassigned"),
  UNASSIGNED_76("76", "Unassigned"),
  UNASSIGNED_77("77", "Unassigned"),
  UNASSIGNED_78("78", "Unassigned"),
  UNASSIGNED_79("79", "Unassigned"),
  UNASSIGNED_80("80", "Unassigned"),
  INDEPENDENT_LABORATORY("81", "Independent Laboratory"),
  UNASSIGNED_82("82", "Unassigned"),
  UNASSIGNED_83("83", "Unassigned"),
  UNASSIGNED_84("84", "Unassigned"),
  UNASSIGNED_85("85", "Unassigned"),
  UNASSIGNED_86("86", "Unassigned"),
  UNASSIGNED_87("87", "Unassigned"),
  UNASSIGNED_88("88", "Unassigned"),
  UNASSIGNED_89("89", "Unassigned"),
  UNASSIGNED_90("90", "Unassigned"),
  UNASSIGNED_91("91", "Unassigned"),
  UNASSIGNED_92("92", "Unassigned"),
  UNASSIGNED_93("93", "Unassigned"),
  UNASSIGNED_94("94", "Unassigned"),
  UNASSIGNED_95("95", "Unassigned"),
  UNASSIGNED_96("96", "Unassigned"),
  UNASSIGNED_97("97", "Unassigned"),
  UNASSIGNED_98("98", "Unassigned"),
  OTHER("99", "Other Place of Service");

  private final String code;
  private final String display;

  PlaceOfService(String code, String display) {
    this.code = code;
    this.display = display;
  }

    public static PlaceOfService fromCode(String code) {
    if (code == null) {
      return null;
    }
    for (PlaceOfService placeOfService : values()) {
      if (placeOfService.code.equals(code)) {
        return placeOfService;
      }
    }
    return null;
  }
}
