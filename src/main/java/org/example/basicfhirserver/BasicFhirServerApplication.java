package org.example.basicfhirserver;

import ca.uhn.fhir.rest.param.StringParam;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BasicFhirServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicFhirServerApplication.class, args);
    }

}
