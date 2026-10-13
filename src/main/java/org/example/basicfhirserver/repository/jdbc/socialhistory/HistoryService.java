package org.example.basicfhirserver.repository.jdbc.socialhistory;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;

public interface HistoryService {

  List<HistoryDBRecord> findHistoryById(UUID uuid);

  List<HistoryDBRecord> findHistory(ObservationSearchQuery searchQuery);
}
