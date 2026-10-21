package org.example.basicfhirserver.repository.jdbc.insurance;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CoverageDBRecord {
  Long id;
  String type;
  String provider;
  String planName;
  String policyNumber;
  String policyType;
  String groupNumber;
  String subscriberLname;
  String subscriberFname;
  String subscriberMname;
  LocalDateTime subscriberDob;
  String subscriberSsn;
  String subscriberSex;
  String subscriberStreet;
  String subscriberCity;
  String subscriberState;
  String subscriberPostalCode;
  String subscriberCountry;
  String subscriberPhone;
  String subscriberRelationship;
  String subscriberEmployer;
  String subscriberEmployerStreet;
  String subscriberEmployerCity;
  String subscriberEmployerState;
  String subscriberEmployerPostalCode;
  String subscriberEmployerCountry;
  LocalDateTime date;
  LocalDateTime dateEnd;
  Long pid;
  String acceptAssignment;
  String copay;
  UUID uuid;

  UUID puuid;
  UUID insureruuid;
  Integer insTypeCode;
}
