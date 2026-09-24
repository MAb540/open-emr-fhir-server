package org.example.basicfhirserver.query.resources.practitioner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PractitionerSearchQuery {

    private String practitionerId;
    private SearchValue<String> name;
    private String identifier;
    private Integer count;
    private Integer offset;

}
