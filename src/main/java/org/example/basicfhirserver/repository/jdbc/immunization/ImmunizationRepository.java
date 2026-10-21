package org.example.basicfhirserver.repository.jdbc.immunization;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchQuery;
import org.springframework.data.domain.Page;

public interface ImmunizationRepository {
  List<ImmunizationDBRecord> findById(UUID uuid);

  Page<ImmunizationDBRecord> find(ImmunizationSearchQuery immunizationSearchQuery);
}
