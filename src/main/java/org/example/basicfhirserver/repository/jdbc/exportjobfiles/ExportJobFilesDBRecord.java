package org.example.basicfhirserver.repository.jdbc.exportjobfiles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExportJobFilesDBRecord {
    private UUID jobUuid;
    private String resourceType;
    private String fileId;
    private LocalDateTime createdAt;
}
