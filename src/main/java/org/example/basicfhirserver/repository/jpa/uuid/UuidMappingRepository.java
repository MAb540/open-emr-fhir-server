package org.example.basicfhirserver.repository.jpa.uuid;

import org.example.basicfhirserver.domain.entities.UuidMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UuidMappingRepository extends JpaRepository<UuidMappingEntity, Long>,
        JpaSpecificationExecutor<UuidMappingEntity> {
}
