package org.example.basicfhirserver.repository.jpa.patient;

import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface PatientRepository
        extends JpaRepository<LegacyPatientEntity, Long>,
        JpaSpecificationExecutor<LegacyPatientEntity> {
}