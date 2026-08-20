package org.example.basicfhirserver.provider.validator;

import ca.uhn.fhir.validation.FhirValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.Resource;

@Service
@RequiredArgsConstructor
public class FhirResponseValidationService {

    private final FhirValidator validator;

    public ValidationResult validate(Resource resource) {
        return validator.validateWithResult(resource);
    }

}
