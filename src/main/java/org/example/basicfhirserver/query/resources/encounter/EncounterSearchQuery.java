package org.example.basicfhirserver.query.resources.encounter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncounterSearchQuery {
    private List<String> encounterId;
    private List<String> patientId;
    private SearchValue<LocalDateTime> date;
    private Integer count;
    private Integer offset;
}