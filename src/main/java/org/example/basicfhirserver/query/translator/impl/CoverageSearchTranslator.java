package org.example.basicfhirserver.query.translator.impl;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

import org.example.basicfhirserver.query.resources.coverage.CoverageSearchCriteria;
import org.example.basicfhirserver.query.resources.coverage.CoverageSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class CoverageSearchTranslator
    implements SearchTranslator<CoverageSearchQuery, CoverageSearchCriteria> {

  @Override
  public CoverageSearchQuery translate(CoverageSearchCriteria criteria) {
    return CoverageSearchQuery.builder()
        .id(token(criteria.getId()))
        .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
        .status(token(criteria.getStatus()))
        .count(criteria.getCount())
        .offset(criteria.getOffset())
        .build();
  }
}
