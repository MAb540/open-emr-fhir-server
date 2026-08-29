package org.example.basicfhirserver.query.resources.medicationrequest;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicationRequestSearchCriteria {
    private TokenOrListParam intent;
    private ReferenceParam patient;
    private StringParam status;
}

