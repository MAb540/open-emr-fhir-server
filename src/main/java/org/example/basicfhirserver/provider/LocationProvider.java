package org.example.basicfhirserver.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.UUID;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.location.LocationSearchCriteria;
import org.example.basicfhirserver.query.translator.impl.LocationSearchTranslator;
import org.example.basicfhirserver.service.LocationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Location;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_LOCATION,
    supported = {ProfilesConstants.HL7_US_CORE_LOCATION})
public class LocationProvider implements IResourceProvider {

  private final LocationService locationService;
  private final LocationSearchTranslator locationSearchTranslator;

  public LocationProvider(
      LocationService locationService, LocationSearchTranslator locationSearchTranslator) {
    this.locationService = locationService;
    this.locationSearchTranslator = locationSearchTranslator;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Location.class;
  }

  @Read()
  public Location getResourceById(@IdParam IdType theId) {
    return locationService.findById(UUID.fromString(theId.getIdPart()));
  }

  @Search()
  public IBundleProvider searchLocation(
      @OptionalParam(name = Location.SP_RES_ID) TokenParam id,
      @OptionalParam(name = Location.SP_NAME) StringParam name,
      @OptionalParam(name = Location.SP_ADDRESS) StringParam address,
      @OptionalParam(name = Location.SP_ADDRESS_CITY) StringParam addressCity,
      @OptionalParam(name = Location.SP_ADDRESS_STATE) StringParam addressState,
      @OptionalParam(name = Location.SP_ADDRESS_POSTALCODE) StringParam addressPostalCode,
      @OptionalParam(name = Location.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @Count Integer count,
      @Offset Integer offset) {
    LocationSearchCriteria criteria =
        LocationSearchCriteria.builder()
            .id(id)
            .name(name)
            .address(address)
            .addressCity(addressCity)
            .addressState(addressState)
            .addressPostalCode(addressPostalCode)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .build();

    var locationSearchQuery = locationSearchTranslator.translate(criteria);
    SearchResult searchResult = locationService.search(locationSearchQuery);

    return new BundleProvider(
        searchResult.primaryResources(),
        searchResult.includedResources(),
        searchResult.totalElements(),
        searchResult.currentOffset(),
        searchResult.primaryResources().size());
  }
}
