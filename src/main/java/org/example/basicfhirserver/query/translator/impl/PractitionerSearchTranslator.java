package org.example.basicfhirserver.query.translator.impl;

import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchCriteria;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.query.translator.SearchTranslator;
import org.springframework.stereotype.Component;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.stringMatch;
import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.token;


@Component
public class PractitionerSearchTranslator implements SearchTranslator<PractitionerSearchQuery, PractitionerSearchCriteria> {

    @Override
    public PractitionerSearchQuery translate(PractitionerSearchCriteria criteria) {

        return PractitionerSearchQuery.builder()
                .name(stringMatch(criteria.getName()))
                .identifier(token(criteria.getIdentifier()))
                .build();
    }

}
