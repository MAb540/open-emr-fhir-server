package org.example.basicfhirserver.provider.parsers;

import java.util.List;

public record ParsedExportRequest(
        List<String> resourcesToExport,
       String parsedSince
) {}