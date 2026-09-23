package org.example.basicfhirserver.query.resources.condition;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConditionSearchCriteria {
    private ReferenceParam patient;
    private TokenParam category;
    private Integer count;
    private Integer offset;
}
