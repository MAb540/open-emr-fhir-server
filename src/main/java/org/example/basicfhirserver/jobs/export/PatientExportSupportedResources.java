package org.example.basicfhirserver.jobs.export;

import lombok.Getter;

@Getter
public enum PatientExportSupportedResources {
  PATIENT("Patient"),
  OBSERVATION("Observation"),
  CONDITION("Condition"),
  ENCOUNTER("Encounter");

  private final String value;

  PatientExportSupportedResources(String value) {
    this.value = value;
  }
}
