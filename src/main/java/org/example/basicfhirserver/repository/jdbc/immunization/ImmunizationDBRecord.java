package org.example.basicfhirserver.repository.jdbc.immunization;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImmunizationDBRecord {
  public Long id;
  public UUID uuid;
  public UUID patientPuuid;
  public LocalDateTime administeredDate;
  public String cvxCode;
  public String cvxCodeText;
  public String manufacturer;
  public String lotNumber;
  public boolean addedErroneously;
  public Long administeredById;
  public String administeredBy;
  public LocalDate educationDate;
  public String note;
  public LocalDateTime createDate;
  public LocalDateTime updateDate;
  public Integer amountAdministered;
  public String amountAdministeredUnit;
  public LocalDate expirationDate;
  public String route;
  public String administrationSite;
  public String siteDisplay;
  public String siteCode;
  public String completionStatus;
  public String reasonCode;
  public String refusalReason;
  public String refusalReasonCodes;
  public String refusalReasonCdcNipCode;
  public String refusalReasonDescription;
  public UUID providerUuid;
  public String providerNpi;
  public String providerUsername;
  public boolean primarySource;
  public UUID encounterEuuid;
  public UUID facilityUuid;
  public UUID facilityLocationUuid;
}
