package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.repository.jpa.patient.PatientRepository;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    properties = {
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.sql.init.mode=always",
      "jobrunr.background-job-server.enabled=false",
      "jobrunr.dashboard.enabled=false"
    })
@Testcontainers
class PatientResourceProviderIntegrationTest {

//  @Container @ServiceConnection
//  static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>("mariadb:11.4");

  @Container @ServiceConnection
  static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>("mariadb:11.4");

  @Autowired private PatientResourceProvider provider;

  @Autowired private PatientRepository patientRepository;

  private UUID patientUuid;

  @BeforeEach
  void setUp() {
    patientRepository.deleteAll();
    patientUuid = UUID.randomUUID();
    patientRepository.save(
        LegacyPatientEntity.builder()
            .uuid(patientUuid)
            .fname("John")
            .lname("Doe")
            .ss("123-45-6789")
            .dob(LocalDate.of(1990, 1, 15))
            .sex("M")
            .build());
  }

  @Test
  void getResourceById_returnsMappedPatient() {
    Patient result = provider.getResourceById(new IdType(patientUuid.toString()));

    assertThat(result.getIdElement().getIdPart()).isEqualTo(patientUuid.toString());
    assertThat(result.getNameFirstRep().getFamily()).isEqualTo("Doe");
    assertThat(result.getNameFirstRep().getGivenAsSingleString()).isEqualTo("John");
    assertThat(result.getGender()).isEqualTo(AdministrativeGender.MALE);
    assertThat(result.getIdentifierFirstRep().getValue()).isEqualTo("123-45-6789");
  }

  @Test
  void getResourceById_throwsWhenPatientNotFound() {
    UUID missingUuid = UUID.randomUUID();

    assertThatThrownBy(() -> provider.getResourceById(new IdType(missingUuid.toString())))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void searchPatients_returnsAllSeededPatients() {
    IBundleProvider bundle =
        provider.searchPatients(null, null, null, null, null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(bundle.getResources(0, 10)).hasSize(1);
  }

  @Test
  void searchPatients_filtersById() {
    TokenParam id = new TokenParam(patientUuid.toString());

    IBundleProvider bundle =
        provider.searchPatients(id, null, null, null, null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    Patient patient = firstPatient(bundle);
    assertThat(patient.getIdElement().getIdPart()).isEqualTo(patientUuid.toString());
  }

  @Test
  void searchPatients_filtersByFamilyName() {
    patientRepository.save(
        LegacyPatientEntity.builder()
            .uuid(UUID.randomUUID())
            .fname("Jane")
            .lname("Smith")
            .ss("987-65-4321")
            .dob(LocalDate.of(1985, 5, 20))
            .sex("F")
            .build());

    StringParam family = new StringParam("Doe");

    IBundleProvider bundle =
        provider.searchPatients(null, null, null, family, null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(firstPatient(bundle).getNameFirstRep().getFamily()).isEqualTo("Doe");
  }

  @Test
  void searchPatients_filtersByIdentifier() {
    TokenParam identifier = new TokenParam("123-45-6789");

    IBundleProvider bundle =
        provider.searchPatients(null, null, identifier, null, null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(firstPatient(bundle).getNameFirstRep().getFamily()).isEqualTo("Doe");
  }

  private Patient firstPatient(IBundleProvider bundle) {
    List<IBaseResource> resources = bundle.getResources(0, 10);
    return (Patient) resources.get(0);
  }
}
