package org.example.basicfhirserver.repository.jdbc.exportjobfiles;

import java.util.List;

public interface ExportJobFilesRepository {
  List<ExportJobFilesDBRecord> find();
}
