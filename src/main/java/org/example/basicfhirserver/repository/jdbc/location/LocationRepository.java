package org.example.basicfhirserver.repository.jdbc.location;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.location.LocationSearchQuery;
import org.springframework.data.domain.Page;

public interface LocationRepository {
  List<LocationDBRecord> findById(UUID uuid);

  Page<LocationDBRecord> find(LocationSearchQuery locationSearchQuery);
}
