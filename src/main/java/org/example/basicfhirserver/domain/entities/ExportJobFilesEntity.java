package org.example.basicfhirserver.domain.entities;

import jakarta.persistence.*;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "jobrunr_export_job_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportJobFilesEntity {

  @Id
  @Column(name = "id")
  @GeneratedValue
  @JdbcTypeCode(Types.BINARY)
  private UUID id;

  @Column(name = "job_id")
  @JdbcTypeCode(Types.BINARY)
  private UUID jobUuid;

  @Column(name = "resource_type")
  private String resourceType;

  @Column(name = "file_id")
  private String fileId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
