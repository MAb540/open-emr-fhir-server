package org.example.basicfhirserver.query.resources.diagnosticreport;

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
public class DiagnosticReportSearchCriteria {
    private TokenParam id;
    private ReferenceParam patient;
    private DateParam date;
    private TokenOrListParam codes;
    private Integer count;
    private Integer offset;
}
