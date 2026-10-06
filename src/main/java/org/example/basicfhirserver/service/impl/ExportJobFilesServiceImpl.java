package org.example.basicfhirserver.service.impl;

import org.example.basicfhirserver.domain.entities.ExportJobFilesEntity;
import org.example.basicfhirserver.repository.jpa.exportjobfiles.ExportJobFilesRepository;
import org.example.basicfhirserver.service.ExportJobFilesService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExportJobFilesServiceImpl implements ExportJobFilesService {

    private final ExportJobFilesRepository exportJobFilesRepository;

    public ExportJobFilesServiceImpl(ExportJobFilesRepository exportJobFilesRepository) {
        this.exportJobFilesRepository = exportJobFilesRepository;
    }

    @Override
    public List<ExportJobFilesEntity> findById(UUID uuid) {
        Specification<ExportJobFilesEntity> spec = (root, query, cb) ->
                cb.equal(root.get("jobUuid"), uuid);
        return exportJobFilesRepository.findAll(spec);
    }

    @Override
    public List<ExportJobFilesEntity> find() {
        return exportJobFilesRepository.findAll();
    }

    @Override
    public List<ExportJobFilesEntity> findByFileId(String fileId) {
        Specification<ExportJobFilesEntity> spec = (root, query, cb) ->
                cb.equal(root.get("fileId"), fileId);
        return exportJobFilesRepository.findAll(spec);
    }

    @Override
    public ExportJobFilesEntity save(ExportJobFilesEntity exportJobFilesEntity) {
        return exportJobFilesRepository.save(exportJobFilesEntity);
    }

}
