package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jpa.patient.PatientRepository;
import org.example.basicfhirserver.repository.jpa.patient.specs.PatientSpecifications;
import org.example.basicfhirserver.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class LegacyPatientDBServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public LegacyPatientDBServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public LegacyPatientEntity findById(UUID uuid) {
        Specification<LegacyPatientEntity> spec = (root, query, cb) ->
                cb.equal(root.get("uuid"), uuid);
        Optional<LegacyPatientEntity> legacyPatientEntity = patientRepository.findOne(spec);

        if(legacyPatientEntity.isEmpty()){
            throw new ResourceNotFoundException("Patient with given ID " + uuid + " not found.");
        }

        return legacyPatientEntity.get();
    }


    @Override
    public Page<LegacyPatientEntity> find(PatientSearchQuery patientSearchQuery) {
        Specification<LegacyPatientEntity> spec =
                PatientSpecifications.from(patientSearchQuery);

        int limit = (patientSearchQuery.getCount() != null) ? patientSearchQuery.getCount() : 5;
        int offset = (patientSearchQuery.getOffset() != null) ? patientSearchQuery.getOffset() : 0;

        Pageable pageable =
                PageRequest.of(
                        offset / limit,
                        limit
                );

        return patientRepository.findAll(spec, pageable);
    }
}
