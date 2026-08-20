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

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping for the openEMR {@code form_vitals} table.
 */
@Entity
@Table(name = "form_vitals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormVitalsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    @JdbcTypeCode(Types.BINARY)
    private UUID uuid;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "pid")
    private Long pid;

    @Column(name = "user")
    private String user;

    @Column(name = "groupname")
    private String groupname;

    @Column(name = "authorized")
    private Integer authorized;

    @Column(name = "activity")
    private Integer activity;

    @Column(name = "bps")
    private String bps;

    @Column(name = "bpd")
    private String bpd;

    @Column(name = "weight")
    private BigDecimal weight;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "temperature")
    private BigDecimal temperature;

    @Column(name = "temp_method")
    private String tempMethod;

    @Column(name = "pulse")
    private BigDecimal pulse;

    @Column(name = "respiration")
    private BigDecimal respiration;

    @Column(name = "note")
    private String note;

    @Column(name = "BMI")
    private BigDecimal bmi;

    @Column(name = "BMI_status")
    private String bmiStatus;

    @Column(name = "waist_circ")
    private BigDecimal waistCirc;

    @Column(name = "head_circ")
    private BigDecimal headCirc;

    @Column(name = "oxygen_saturation")
    private BigDecimal oxygenSaturation;

    @Column(name = "oxygen_flow_rate")
    private BigDecimal oxygenFlowRate;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "ped_weight_height")
    private BigDecimal pedWeightHeight;

    @Column(name = "ped_bmi")
    private BigDecimal pedBmi;

    @Column(name = "ped_head_circ")
    private BigDecimal pedHeadCirc;

    @Column(name = "inhaled_oxygen_concentration")
    private BigDecimal inhaledOxygenConcentration;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
