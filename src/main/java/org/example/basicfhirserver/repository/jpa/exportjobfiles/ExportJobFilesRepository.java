package org.example.basicfhirserver.repository.jpa.exportjobfiles;

import java.util.UUID;
import org.example.basicfhirserver.domain.entities.ExportJobFilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportJobFilesRepository
    extends JpaRepository<ExportJobFilesEntity, UUID>,
        JpaSpecificationExecutor<ExportJobFilesEntity> {}
