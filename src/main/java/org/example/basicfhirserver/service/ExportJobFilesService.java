package org.example.basicfhirserver.service;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.domain.entities.ExportJobFilesEntity;

public interface ExportJobFilesService {

  List<ExportJobFilesEntity> findById(UUID uuid);

  List<ExportJobFilesEntity> find();

  List<ExportJobFilesEntity> findByFileId(String fileId);

  ExportJobFilesEntity save(ExportJobFilesEntity exportJobFilesEntity);
}
