package org.example.basicfhirserver.jobs.export;

import lombok.Getter;

@Getter
public enum FhirExportSupportedResources {
  PATIENT("Patient"),
  OBSERVATION("Observation"),
  CONDITION("Condition"),
  ENCOUNTER("Encounter");

  private final String value;

  FhirExportSupportedResources(String value) {
    this.value = value;
  }
}
