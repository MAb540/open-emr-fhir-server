package org.example.basicfhirserver.service.impl.coverage;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.CoverageMapper;
import org.example.basicfhirserver.query.resources.coverage.CoverageSearchQuery;
import org.example.basicfhirserver.repository.jdbc.insurance.CoverageDBRecord;
import org.example.basicfhirserver.repository.jdbc.insurance.CoverageRepository;
import org.example.basicfhirserver.service.CoverageService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coverage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class CoverageServiceImpl implements CoverageService {

  private final CoverageRepository coverageRepository;
  private final CoverageMapper coverageMapper;

  public CoverageServiceImpl(CoverageRepository coverageRepository, CoverageMapper coverageMapper) {
    this.coverageRepository = coverageRepository;
    this.coverageMapper = coverageMapper;
  }

  @Override
  public Coverage findById(UUID uuid) {
    List<CoverageDBRecord> coverageDBRecords = coverageRepository.findById(uuid);
    if (coverageDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Coverage with given ID " + uuid + " not found.");
    }
    return coverageMapper.toR4(coverageDBRecords.get(0));
  }

  @Override
  public SearchResult search(CoverageSearchQuery coverageSearchQuery) {
    Page<CoverageDBRecord> coverageDBRecords = coverageRepository.find(coverageSearchQuery);

    List<IBaseResource> primaryCoverages =
        coverageDBRecords.getContent().stream().<IBaseResource>map(coverageMapper::toR4).toList();

    int currentOffset =
        coverageSearchQuery.getOffset() != null ? coverageSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryCoverages, List.of(), (int) coverageDBRecords.getTotalElements(), currentOffset);
  }
}
