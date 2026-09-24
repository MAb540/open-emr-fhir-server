package org.example.basicfhirserver.repository.jdbc.user;

import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDBRecord> findById(UUID uuid);

    Page<UserDBRecord> find(PractitionerSearchQuery practitionerSearchQuery);


}
