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

import java.time.LocalDateTime;

/**
 * JPA entity mapping for the openEMR {@code form_soap} table.
 */
@Entity
@Table(name = "form_soap")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormSoapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @Column(name = "subjective")
    private String subjective;

    @Column(name = "objective")
    private String objective;

    @Column(name = "assessment")
    private String assessment;

    @Column(name = "plan")
    private String plan;
}
