package org.example.basicfhirserver.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping for the openEMR {@code form_clinical_notes} table.
 */
@Entity
@Table(name = "form_clinical_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormClinicalNotesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID uuid;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "pid")
    private Long pid;

    @Column(name = "encounter")
    private String encounter;

    @Column(name = "user")
    private String user;

    @Column(name = "groupname")
    private String groupname;

    @Column(name = "authorized")
    private Integer authorized;

    @Column(name = "activity")
    private Integer activity;

    @Column(name = "code")
    private String code;

    @Column(name = "codetext")
    private String codetext;

    @Column(name = "description")
    private String description;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "clinical_notes_type")
    private String clinicalNotesType;

    @Column(name = "clinical_notes_category")
    private String clinicalNotesCategory;

    @Column(name = "note_related_to")
    private String noteRelatedTo;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
