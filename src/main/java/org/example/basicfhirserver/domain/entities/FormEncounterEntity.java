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
 * JPA entity mapping for the openEMR {@code form_encounter} table.
 */
@Entity
@Table(name = "form_encounter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormEncounterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID uuid;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "reason")
    private String reason;

    @Column(name = "facility")
    private String facility;

    @Column(name = "facility_id")
    private Integer facilityId;

    @Column(name = "pid")
    private Long pid;

    @Column(name = "encounter")
    private Long encounter;

    @Column(name = "onset_date")
    private LocalDateTime onsetDate;

    @Column(name = "sensitivity")
    private String sensitivity;

    @Column(name = "billing_note")
    private String billingNote;

    @Column(name = "pc_catid")
    private Integer pcCatid;

    @Column(name = "last_level_billed")
    private Integer lastLevelBilled;

    @Column(name = "last_level_closed")
    private Integer lastLevelClosed;

    @Column(name = "last_stmt_date")
    private LocalDate lastStmtDate;

    @Column(name = "stmt_count")
    private Integer stmtCount;

    @Column(name = "provider_id")
    private Integer providerId;

    @Column(name = "supervisor_id")
    private Integer supervisorId;

    @Column(name = "invoice_refno")
    private String invoiceRefno;

    @Column(name = "referral_source")
    private String referralSource;

    @Column(name = "billing_facility")
    private Integer billingFacility;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "pos_code")
    private Integer posCode;

    @Column(name = "parent_encounter_id")
    private Long parentEncounterId;

    @Column(name = "class_code")
    private String classCode;

    @Column(name = "shift")
    private String shift;

    @Column(name = "voucher_number")
    private String voucherNumber;

    @Column(name = "discharge_disposition")
    private String dischargeDisposition;

    @Column(name = "encounter_type_code")
    private String encounterTypeCode;

    @Column(name = "encounter_type_description")
    private String encounterTypeDescription;

    @Column(name = "referring_provider_id")
    private Integer referringProviderId;

    @Column(name = "date_end")
    private LocalDateTime dateEnd;

    @Column(name = "in_collection")
    private Integer inCollection;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "ordering_provider_id")
    private Integer orderingProviderId;
}
