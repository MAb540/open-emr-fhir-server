package org.example.basicfhirserver.repository.jpa.forms;

import org.example.basicfhirserver.domain.entities.FormsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FormRepository extends JpaRepository<FormsEntity, Long>,
        JpaSpecificationExecutor<FormsEntity> {
}

