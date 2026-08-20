package org.example.basicfhirserver.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.io.Serializable;
import java.sql.Types;
import java.util.UUID;

/**
 * JPA entity mapping for the openEMR {@code form_vitals_calculation_form_vitals}
 * join table. Composite primary key ({@code fvc_uuid}, {@code vitals_id}).
 */
@Entity
@Table(name = "form_vitals_calculation_form_vitals")
@IdClass(FormVitalsCalculationFormVitalsEntity.PrimaryKey.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormVitalsCalculationFormVitalsEntity {

    @Id
    @Column(name = "fvc_uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID fvcUuid;

    @Id
    @Column(name = "vitals_id")
    private Long vitalsId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrimaryKey implements Serializable {
        private UUID fvcUuid;
        private Long vitalsId;
    }
}
