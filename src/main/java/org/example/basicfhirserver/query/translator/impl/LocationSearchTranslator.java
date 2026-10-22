package org.example.basicfhirserver.query.translator.impl;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.stringMatch;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;

import java.time.ZoneId;
import org.example.basicfhirserver.query.resources.location.LocationSearchCriteria;
import org.example.basicfhirserver.query.resources.location.LocationSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

@Component
public class LocationSearchTranslator
    implements SearchTranslator<LocationSearchQuery, LocationSearchCriteria> {

  @Override
  public LocationSearchQuery translate(LocationSearchCriteria criteria) {
    return LocationSearchQuery.builder()
        .id(token(criteria.getId()))
        .name(stringMatch(criteria.getName()))
        .address(stringMatch(criteria.getAddress()))
        .addressCity(stringMatch(criteria.getAddressCity()))
        .addressState(stringMatch(criteria.getAddressState()))
        .addressPostalCode(stringMatch(criteria.getAddressPostalCode()))
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
