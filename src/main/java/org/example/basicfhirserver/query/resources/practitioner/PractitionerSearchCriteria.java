package org.example.basicfhirserver.query.resources.practitioner;

import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PractitionerSearchCriteria {

    private TokenParam id;
    private StringParam name;
    private TokenParam identifier;
    private Integer count;
    private Integer offset;

}
