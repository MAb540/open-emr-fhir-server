package org.example.basicfhirserver.query.resources.medication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicationSearchCriteria {
    private Integer count;
    private Integer offset;
}
