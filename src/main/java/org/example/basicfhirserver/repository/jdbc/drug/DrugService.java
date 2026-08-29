package org.example.basicfhirserver.repository.jdbc.drug;

import java.util.List;
import java.util.UUID;

public interface DrugService {

    List<DrugDBRecord> findById(UUID uuid);

    List<DrugDBRecord> find();

}
