package org.example.basicfhirserver.query.translator.impl;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

import java.time.ZoneId;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchCriteria;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class ImmunizationSearchTranslator
    implements SearchTranslator<ImmunizationSearchQuery, ImmunizationSearchCriteria> {

  @Override
  public ImmunizationSearchQuery translate(ImmunizationSearchCriteria criteria) {
    return ImmunizationSearchQuery.builder()
        .id(token(criteria.getId()))
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
        .count(criteria.getCount())
        .offset(criteria.getOffset())
        .build();
  }
}
