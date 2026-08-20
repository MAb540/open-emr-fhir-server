package org.example.basicfhirserver.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "uuid_mapping",
        indexes = {
                @Index(name = "idx_uuid", columnList = "uuid"),
                @Index(name = "idx_resource", columnList = "resource"),
                @Index(name = "idx_table", columnList = "`table`"),
                @Index(name = "idx_target_uuid", columnList = "target_uuid")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UuidMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID uuid;

    @Column(name = "resource", nullable = false)
    private String resource = "";

    // Escaped because 'table' is an SQL reserved keyword
    @Column(name = "`table`", nullable = false)
    private String table = "";

    @Column(name = "target_uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID targetUuid;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "resource_path")
    private String resourcePath;

}

