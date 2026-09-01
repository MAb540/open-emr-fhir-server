package org.example.basicfhirserver.query.resources.allergyintolerance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllergyIntoleranceSearchQuery {
    private String patientId;
}

