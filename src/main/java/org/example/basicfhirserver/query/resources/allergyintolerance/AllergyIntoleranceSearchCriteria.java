package org.example.basicfhirserver.query.resources.allergyintolerance;

import ca.uhn.fhir.rest.param.ReferenceParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllergyIntoleranceSearchCriteria {
    private ReferenceParam patient;
}

