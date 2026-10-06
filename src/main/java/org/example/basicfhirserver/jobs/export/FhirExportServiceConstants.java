package org.example.basicfhirserver.jobs.export;

public class FhirExportServiceConstants {
    public static final String ExportPollEndpoint = "export-poll-status";
    public static final String ExportPollEndpointWithParams = "/" + ExportPollEndpoint + "?jobId=";
}
