package org.example.basicfhirserver.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "uuid_registry")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UuidRegistryEntity {

    @Id
    @Column(name = "uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID uuid;

    @Column(name = "table_name", nullable = false)
    private String tableName = "";

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "table_id", nullable = false)
    private String tableId = "";

    @Column(name = "table_vertical", nullable = false)
    private String tableVertical = "";

    @Column(name = "couchdb", nullable = false)
    private String couchdb = "";

    @Column(name = "document_drive", nullable = false)
    private Integer documentDrive = 0;

    @Column(name = "mapped", nullable = false)
    private Integer mapped = 0;
}
