package org.example.basicfhirserver.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class FhirValidationConfig {

    @Bean
    public NpmPackageValidationSupport npmPackageValidationSupport(FhirContext ctx)
            throws Exception {

        NpmPackageValidationSupport support =
                new NpmPackageValidationSupport(ctx);

        support.loadPackageFromClasspath(
                "classpath:hl7.fhir.us.core-9.0.0.tgz"
        );

        return support;
    }

    @Bean
    public ValidationSupportChain validationSupportChain(
            FhirContext ctx,
            NpmPackageValidationSupport npmSupport
    ) {

        return new ValidationSupportChain(
                new DefaultProfileValidationSupport(ctx),
                new InMemoryTerminologyServerValidationSupport(ctx),
                npmSupport
        );

    }

    @Bean
    public FhirInstanceValidator fhirInstanceValidator(
            FhirContext ctx,
            ValidationSupportChain chain
    ) {

        return new FhirInstanceValidator(chain);
    }

    @Bean
    public FhirValidator fhirValidator(
            FhirContext ctx,
            FhirInstanceValidator validator
    ) {

        FhirValidator fhirValidator = ctx.newValidator();

        fhirValidator.registerValidatorModule(validator);

        return fhirValidator;

    }

    @Bean
    public RequestValidatingInterceptor requestValidatingInterceptor(
            FhirInstanceValidator validator
    ) {

        RequestValidatingInterceptor interceptor =
                new RequestValidatingInterceptor();

        interceptor.setValidatorModules(
                Collections.<IValidatorModule>singletonList(validator)
        );

        interceptor.setFailOnSeverity(ResultSeverityEnum.ERROR);

        return interceptor;

    }

}
