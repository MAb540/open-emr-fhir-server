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
 * JPA entity mapping for the openEMR {@code forms} table.
 */
@Entity
@Table(name = "forms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "encounter")
    private Long encounter;

    @Column(name = "form_name")
    private String formName;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "pid")
    private Long pid;

    @Column(name = "user")
    private String user;

    @Column(name = "groupname")
    private String groupname;

    @Column(name = "authorized")
    private Integer authorized;

    @Column(name = "deleted")
    private Integer deleted;

    @Column(name = "formdir")
    private String formdir;

    @Column(name = "therapy_group_id")
    private Integer therapyGroupId;

    @Column(name = "issue_id")
    private Long issueId;

    @Column(name = "provider_id")
    private Long providerId;
}
