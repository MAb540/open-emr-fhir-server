package org.example.basicfhirserver.query.resources.observation;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ObservationSearchCriteria {

    private ReferenceParam patient;
    private TokenParam category;
    private TokenOrListParam codes;
    private DateParam date;
    private DateParam lastUpdated;

}
