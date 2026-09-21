package org.example.basicfhirserver.provider.utils;


import ca.uhn.fhir.rest.api.server.IBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BundleProvider implements IBundleProvider {

    private final List<IBaseResource> primaryResources;
    private final List<IBaseResource> includedResources;
    private final int totalSize;
    private final int currentPageOffset;
    private final int currentPageSize;

    public BundleProvider(
            List<IBaseResource> primaryResources,
            List<IBaseResource> includedResources,
            int totalSize,
            int currentPageOffset,
            int currentPageSize
    ) {
        this.primaryResources = primaryResources;
        this.includedResources = includedResources;
        this.totalSize = totalSize;
        this.currentPageOffset = currentPageOffset;
        this.currentPageSize = currentPageSize;
    }

    @NonNull
    @Override
    public List<IBaseResource> getResources(int fromIndex, int toIndex) {

        List<IBaseResource> resources = new ArrayList<>();
        resources.addAll(primaryResources);
        resources.addAll(includedResources);
        return resources;
    }

    @Override
    public Integer size() {
        return totalSize;
    }

    @Override
    public Integer getCurrentPageOffset() {
        return currentPageOffset;
    }

    @Override
    public Integer getCurrentPageSize() {
        return currentPageSize;
    }

    @Override
    public IPrimitiveType<Date> getPublished() {
        return null;
    }

    @Override
    public Integer preferredPageSize() {
        return currentPageSize;
    }

    @Override
    public String getUuid() {
        return null;
    }
}