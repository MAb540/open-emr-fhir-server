package org.example.basicfhirserver.provider.validator;

import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FhirResponseValidationService {

  private final FhirValidator validator;

  public ValidationResult validate(Resource resource) {
    return validator.validateWithResult(resource);
  }
}
