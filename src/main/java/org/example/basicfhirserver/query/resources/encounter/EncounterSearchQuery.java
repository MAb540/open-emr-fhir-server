package org.example.basicfhirserver.query.resources.encounter;

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
public class EncounterSearchQuery {
    private String encounterId;
    private String patientId;
    private SearchValue<LocalDateTime> date;
}