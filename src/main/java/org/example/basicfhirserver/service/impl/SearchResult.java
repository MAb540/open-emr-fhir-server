package org.example.basicfhirserver.service.impl;

import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;

public record SearchResult(
    List<IBaseResource> primaryResources,
    List<IBaseResource> includedResources,
    int totalElements,
    int currentOffset) {}
