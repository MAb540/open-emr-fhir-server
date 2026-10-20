package org.example.basicfhirserver.repository.jdbc.insurance;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.coverage.CoverageSearchQuery;
import org.springframework.data.domain.Page;

public interface CoverageRepository {
  List<CoverageDBRecord> findById(UUID uuid);

  Page<CoverageDBRecord> find(CoverageSearchQuery coverageSearchQuery);
}
