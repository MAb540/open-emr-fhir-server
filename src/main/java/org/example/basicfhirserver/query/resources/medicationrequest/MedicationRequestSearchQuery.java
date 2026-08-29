package org.example.basicfhirserver.query.resources.medicationrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicationRequestSearchQuery {
    private String patientId;
    private List<SearchValue<String>> intent;
    private String status;
}

