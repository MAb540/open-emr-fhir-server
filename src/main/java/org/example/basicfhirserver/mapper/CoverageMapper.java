package org.example.basicfhirserver.mapper;

import org.example.basicfhirserver.repository.jdbc.insurance.CoverageDBRecord;
import org.hl7.fhir.r4.model.Coverage;

public interface CoverageMapper extends ResourceMapper<Coverage, CoverageDBRecord> {

  Coverage toR4(CoverageDBRecord coverageDBRecord);
}
