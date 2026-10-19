package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.person.PersonSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.PersonSearchTranslator;
import org.example.basicfhirserver.service.PersonService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonResourceProvider implements IResourceProvider {

  private final PersonService personService;
  private final PersonSearchTranslator personSearchTranslator;

  public PersonResourceProvider(
      PersonService personService, PersonSearchTranslator personSearchTranslator) {
    this.personService = personService;
    this.personSearchTranslator = personSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Person.class;
  }

  @Read()
  public Person getResourceById(@IdParam IdType theId) {
    return personService.findById(UUID.fromString(theId.getIdPart()));
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
    SearchResult searchResult = personService.search(personSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
