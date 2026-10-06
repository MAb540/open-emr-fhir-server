package org.example.basicfhirserver.repository.jpa.exportjobfiles;

import org.example.basicfhirserver.domain.entities.ExportJobFilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExportJobFilesRepository extends JpaRepository<ExportJobFilesEntity, UUID>,
        JpaSpecificationExecutor<ExportJobFilesEntity> {
}
