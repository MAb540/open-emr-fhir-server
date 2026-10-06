package org.example.basicfhirserver.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import java.util.Arrays;
import java.util.List;
import org.example.basicfhirserver.interceptor.CustomSecurityInterceptor;
import org.example.basicfhirserver.provider.*;
import org.hl7.fhir.r4.model.CanonicalType;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirServerConfig {

  private final String SUPPORTED_IG =
      "http://hl7.org/fhir/us/core/ImplementationGuide/hl7.fhir.us.core";

  @Bean
  public ServletRegistrationBean<RestfulServer> fhirServerServlet(
      PatientResourceProvider patientResourceProvider,
      ObservationResourceProvider observationResourceProvider,
      EncounterResourceProvider encounterResourceProvider,
      PractitionerResourceProvider practitionerResourceProvider,
      MedicationProvider medicationProvider,
      MedicationRequestResourceProvider medicationRequestResourceProvider,
      AllergyIntoleranceProvider allergyIntoleranceProvider,
      ConditionResourceProvider conditionResourceProvider,
      DiagnosticReportProvider diagnosticReportProvider,
      BulkExportPollProvider bulkExportPollProvider,
      FhirContextConfig fhirContextConfig,
      RequestValidatingInterceptor validatingInterceptor) {

    FhirContext ctx = fhirContextConfig.fhirContext();
    RestfulServer servlet = new RestfulServer(ctx);

    servlet.setResourceProviders(
        List.of(
            patientResourceProvider,
            observationResourceProvider,
            encounterResourceProvider,
            practitionerResourceProvider,
            medicationProvider,
            medicationRequestResourceProvider,
            allergyIntoleranceProvider,
            diagnosticReportProvider,
            conditionResourceProvider));
    servlet.registerProvider(bulkExportPollProvider);

    CustomSecurityInterceptor customSecurityInterceptor = new CustomSecurityInterceptor();

    ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider metadataProvider =
        new ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider(servlet) {
          @Override
          public org.hl7.fhir.r4.model.CapabilityStatement getServerConformance(
              jakarta.servlet.http.HttpServletRequest theRequest,
              ca.uhn.fhir.rest.api.server.RequestDetails theRequestDetails) {

            org.hl7.fhir.r4.model.CapabilityStatement cs =
                (org.hl7.fhir.r4.model.CapabilityStatement)
                    super.getServerConformance(theRequest, theRequestDetails);

            cs.setPublisher("OpenEMR FHIR Facade Platform");
            cs.setName("US-Core-Compliant-Facade-Engine");
            cs.setImplementationGuide(List.of(new CanonicalType(SUPPORTED_IG)));

            cs.getRestFirstRep()
                .getResource()
                .forEach(
                    resource -> {
                      servlet.getResourceProviders().stream()
                          .filter(
                              p -> p.getResourceType().getSimpleName().equals(resource.getType()))
                          .findFirst()
                          .ifPresent(
                              provider -> {
                                if (provider
                                    .getClass()
                                    .isAnnotationPresent(SupportedProfiles.class)) {
                                  SupportedProfiles anno =
                                      provider.getClass().getAnnotation(SupportedProfiles.class);

                                  if (!anno.profile().isEmpty()) {
                                    resource.setProfile(anno.profile());
                                  }
                                  if (anno.supported().length > 0) {
                                    List<CanonicalType> list =
                                        Arrays.stream(anno.supported())
                                            .map(CanonicalType::new)
                                            .toList();
                                    resource.setSupportedProfile(list);
                                  }
                                }
                              });
                    });
            return cs;
          }
        };

    servlet.setServerConformanceProvider(metadataProvider);
    servlet.registerInterceptor(validatingInterceptor);
    servlet.registerInterceptor(customSecurityInterceptor);

    ServletRegistrationBean<RestfulServer> registration =
        new ServletRegistrationBean<>(servlet, "/fhir/*");
    registration.setName("HAPI-FHIR-Servlet");

    return registration;
  }
}
