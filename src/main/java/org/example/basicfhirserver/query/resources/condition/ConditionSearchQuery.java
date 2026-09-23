package org.example.basicfhirserver.query.resources.condition;

import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConditionSearchQuery {
    private String patientId;
    private String category;
    private Integer count;
    private Integer offset;
}
