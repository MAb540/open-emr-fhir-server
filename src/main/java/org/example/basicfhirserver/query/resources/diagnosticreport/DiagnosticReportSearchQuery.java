package org.example.basicfhirserver.query.resources.diagnosticreport;

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
public class DiagnosticReportSearchQuery {
    private String diagnosticReportId;
    private String patientId;
    private SearchValue<LocalDateTime> date;
    private List<SearchValue<String>> codes;
    private Integer count;
    private Integer offset;
}
