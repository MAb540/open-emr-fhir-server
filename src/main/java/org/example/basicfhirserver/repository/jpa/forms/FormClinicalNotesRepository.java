package org.example.basicfhirserver.repository.jpa.forms;

import org.example.basicfhirserver.domain.entities.FormClinicalNotesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FormClinicalNotesRepository extends JpaRepository<FormClinicalNotesEntity, Long>,
        JpaSpecificationExecutor<FormClinicalNotesEntity> {
}
