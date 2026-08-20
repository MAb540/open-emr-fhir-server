package org.example.basicfhirserver.repository.jdbc.vitals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VitalsUuidMappingDBRecord {
    UUID uuid;
    String resource;
    String table;
    UUID targetUuid;
    String resourcePath;
}
