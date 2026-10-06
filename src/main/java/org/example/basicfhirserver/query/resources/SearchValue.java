package org.example.basicfhirserver.query.resources;

import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class SearchValue<T> {

  private ParamPrefixEnum prefix;

  private boolean contains;

  private boolean exact;

  private String system;

  private T value;
}
