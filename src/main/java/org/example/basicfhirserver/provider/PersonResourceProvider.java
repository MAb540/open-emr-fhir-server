package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Offset;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.PersonMapper;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.person.PersonSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.PersonSearchTranslator;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.service.PersonService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PersonResourceProvider implements IResourceProvider {

  private final PersonService personService;
  private final PersonMapper personMapper;
  private final PersonSearchTranslator personSearchTranslator;

  public PersonResourceProvider(
      PersonService personService,
      PersonMapper personMapper,
      PersonSearchTranslator personSearchTranslator) {
    this.personService = personService;
    this.personMapper = personMapper;
    this.personSearchTranslator = personSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Person.class;
  }

  @Read()
  public Person getResourceById(@IdParam IdType theId) {
    UserDBRecord userDBRecord = personService.findById(UUID.fromString(theId.getIdPart()));
    return personMapper.toR4(userDBRecord);
  }

  @Search()
  public IBundleProvider searchPersons(
      @OptionalParam(name = Person.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Person.SP_NAME) StringParam name,
      @OptionalParam(name = Person.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @Count Integer count,
      @Offset Integer offset) {
    PersonSearchCriteria criteria =
        PersonSearchCriteria.builder()
            .id(id)
            .name(name)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .build();

    var personSearchQuery = personSearchTranslator.translate(criteria);
    Page<UserDBRecord> personDBRecords = personService.find(personSearchQuery);

    List<IBaseResource> primaryPersons =
        personDBRecords.getContent().stream().<IBaseResource>map(personMapper::toR4).toList();

    int currentOffset = offset != null ? offset : 0;
    int currentPageSize = personDBRecords.getContent().size();

    return new BundleProvider(
        primaryPersons,
        List.of(),
        Math.toIntExact(personDBRecords.getTotalElements()),
        currentOffset,
        currentPageSize);
  }
}
