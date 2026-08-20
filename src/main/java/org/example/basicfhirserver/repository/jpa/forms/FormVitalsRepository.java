package org.example.basicfhirserver.repository.jpa.forms;

import org.example.basicfhirserver.domain.entities.FormVitalsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FormVitalsRepository extends JpaRepository<FormVitalsEntity, Long>,
        JpaSpecificationExecutor<FormVitalsEntity> {
}
