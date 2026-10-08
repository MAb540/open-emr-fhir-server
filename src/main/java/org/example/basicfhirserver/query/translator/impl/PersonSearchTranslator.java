package org.example.basicfhirserver.query.translator.impl;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.stringMatch;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

import java.time.ZoneId;
import java.util.List;
import org.example.basicfhirserver.query.resources.person.PersonSearchCriteria;
import org.example.basicfhirserver.query.resources.person.PersonSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class PersonSearchTranslator
    implements SearchTranslator<PersonSearchQuery, PersonSearchCriteria> {

  @Override
  public PersonSearchQuery translate(PersonSearchCriteria criteria) {
    return PersonSearchQuery.builder()
        .personId(criteria.getId() != null ? List.of(token(criteria.getId())) : List.of())
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
