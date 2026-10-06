package org.example.basicfhirserver.provider.parsers;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import jakarta.servlet.http.HttpServletResponse;
import org.example.basicfhirserver.exceptions.BulkExportValidationException;
import org.example.basicfhirserver.query.resources.SearchValue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.example.basicfhirserver.query.translator.utils.TranslatorUtils.date;

public class BulkExportRequestParser {

    private final List<String> supportedResources;

    public BulkExportRequestParser(List<String> supportedResources) {
        this.supportedResources = supportedResources;
    }

    public ParsedExportRequest parseAndValidate(RequestDetails requestDetails) {
        String preferHeader = requestDetails.getHeader("Prefer");
        if (preferHeader == null || !preferHeader.contains("respond-async")) {
            throw new BulkExportValidationException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Missing mandatory header 'Prefer: respond-async'"
            );
        }

        List<String> resourcesToExport = parseTypeParameter(requestDetails);
        String parsedSince = parseSinceParameter(requestDetails);

        return new ParsedExportRequest(resourcesToExport, parsedSince);
    }

    private List<String> parseTypeParameter(RequestDetails requestDetails) {
        String[] typeValues = requestDetails.getParameters().get("_type");
        String rawType = (typeValues != null && typeValues.length > 0) ? typeValues[0] : null;

        if (rawType == null || rawType.isBlank()) {
            return List.copyOf(supportedResources);
        }

        List<String> requested = Arrays.stream(rawType.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for (String resourceType : requested) {
            if (!supportedResources.contains(resourceType)) {
                throw new BulkExportValidationException(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Unsupported resource type in _type parameter: " + resourceType
                );
            }
        }
        return requested;
    }

    private String parseSinceParameter(RequestDetails requestDetails) {
        String[] sinceValues = requestDetails.getParameters().get("_since");
        return (sinceValues != null && sinceValues.length > 0) ? sinceValues[0] : null;

    }

    private String parseOutputParameter(){
        // _outputFormat
        // _until
        return "";
    }


    public static SearchValue<LocalDateTime> parseSinceParameter(String since) {
        SearchValue<LocalDateTime> parsedSince;

        if (since != null && !since.isBlank()) {
            ca.uhn.fhir.rest.param.DateParam dynamicParam = new ca.uhn.fhir.rest.param.DateParam(since);
            if (dynamicParam.getPrefix() == null) {
                dynamicParam.setPrefix(ParamPrefixEnum.GREATERTHAN);
            }
            parsedSince = date(
                    dynamicParam,
                    d -> d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            );
        } else {
            parsedSince = null;
        }
        return parsedSince;
    }

}
