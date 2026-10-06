package org.example.basicfhirserver.query.translator.impl;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

import java.time.ZoneId;
import java.util.List;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchCriteria;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.example.basicfhirserver.query.translator.utils.TranslatorUtils;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticReportSearchTranslator
    implements SearchTranslator<DiagnosticReportSearchQuery, DiagnosticReportSearchCriteria> {
  @Override
  public DiagnosticReportSearchQuery translate(DiagnosticReportSearchCriteria criteria) {
    return DiagnosticReportSearchQuery.builder()
        .diagnosticReportId(token(criteria.getId()))
        .patientId(criteria.getPatient() == null ? null : criteria.getPatient().getIdPart())
        .date(
            date(
                criteria.getDate(),
                d ->
                    d == null
                        ? null
                        : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
        .lastUpdated(
            date(
                criteria.getLastUpdated(),
                d ->
                    d == null
                        ? null
                        : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
        .codes(
            criteria.getCodes() == null
                ? List.of()
                : criteria.getCodes().getValuesAsQueryTokens().stream()
                    .map(TranslatorUtils::tokenWithSystem)
                    .toList())
        .count(criteria.getCount())
        .offset(criteria.getOffset())
        .build();
  }
}
