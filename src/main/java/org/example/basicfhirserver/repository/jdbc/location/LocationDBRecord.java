package org.example.basicfhirserver.repository.jdbc.location;

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
public class LocationDBRecord {
  public UUID tableUuid;
  public String name;
  public String identifier;
  public String identifierType;
  public String street;
  public String city;
  public String postalCode;
  public String state;
  public String countryCode;
  public String phone;
  public String fax;
  public String website;
  public String email;
  public LocalDateTime lastUpdated;
  public String type;
  public String locationRoleType;
  public UUID fhirLocationUuid;
}
