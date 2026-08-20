package org.example.basicfhirserver.query.resources;

import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchValue<T> {

    private ParamPrefixEnum prefix;

    private boolean contains;

    private boolean exact;

    private String system;

    private T value;
}


