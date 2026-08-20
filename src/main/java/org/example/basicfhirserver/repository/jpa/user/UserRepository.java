package org.example.basicfhirserver.repository.jpa.user;

import org.example.basicfhirserver.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository
        extends JpaRepository<UserEntity, Long>,
        JpaSpecificationExecutor<UserEntity> {
}