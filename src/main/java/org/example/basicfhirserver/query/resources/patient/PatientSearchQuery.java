package org.example.basicfhirserver.query.resources.patient;

import lombok.Builder;
import lombok.Data;
import org.example.basicfhirserver.query.resources.SearchValue;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PatientSearchQuery {

    private String patientId;
    private String identifier;
    private SearchValue<String> firstName;
    private SearchValue<String> lastName;
    private SearchValue<String> name;
    private SearchValue<LocalDate> birthDate;
    private SearchValue<LocalDateTime> deathDate;
    private Integer count;
    private Integer offset;
}