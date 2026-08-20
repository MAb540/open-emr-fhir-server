package org.example.basicfhirserver.repository.jpa.forms;

import org.example.basicfhirserver.domain.entities.FormEncounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FormEncounterRepository extends JpaRepository<FormEncounterEntity, Long>,
        JpaSpecificationExecutor<FormEncounterEntity> {
}
