package org.example.basicfhirserver.query.resources.allergyintolerance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllergyIntoleranceSearchQuery {
    private String id;
    private String patientId;
    private SearchValue<LocalDateTime> lastUpdated;
    private Integer count;
    private Integer offset;
}

