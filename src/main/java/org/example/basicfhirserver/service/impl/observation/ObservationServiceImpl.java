package org.example.basicfhirserver.service.impl.observation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.service.ObservationService;
import org.springframework.stereotype.Service;

@Service("ObservationServiceImpl")
public class ObservationServiceImpl implements ObservationService {

  private final List<ObservationProvider> observationProviders;

  public ObservationServiceImpl(List<ObservationProvider> observationProviders) {
    this.observationProviders = observationProviders;
  }

  @Override
  public Optional<CanonicalObservation> findById(UUID uuid) {
    return observationProviders.stream()
        .map(provider -> provider.fetchById(uuid))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  @Override
  public List<CanonicalObservation> find(ObservationSearchQuery observationSearchQuery) {
    return observationProviders.stream()
        .filter(provider -> provider.supports(observationSearchQuery))
        .flatMap(provider -> provider.fetch(observationSearchQuery).stream())
        .toList();
  }
}
