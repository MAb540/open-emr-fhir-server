package org.example.basicfhirserver.query.resources.encounter;

import ca.uhn.fhir.rest.param.DateParam;
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
public class EncounterSearchCriteria {
    private TokenParam id;
    private ReferenceParam patient;
    private DateParam date;
    private Integer count;
    private Integer offset;
}