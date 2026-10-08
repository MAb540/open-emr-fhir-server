package org.example.basicfhirserver.query.translator.impl;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.stringMatch;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

import java.time.ZoneId;
import java.util.List;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchCriteria;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class OrganizationSearchTranslator
    implements SearchTranslator<OrganizationSearchQuery, OrganizationSearchCriteria> {

  @Override
  public OrganizationSearchQuery translate(OrganizationSearchCriteria criteria) {
    return OrganizationSearchQuery.builder()
        .organizationId(criteria.getId() != null ? List.of(token(criteria.getId())) : List.of())
        .name(stringMatch(criteria.getName()))
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
