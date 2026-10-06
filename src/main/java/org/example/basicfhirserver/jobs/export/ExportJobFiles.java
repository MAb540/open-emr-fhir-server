package org.example.basicfhirserver.jobs.export;

import lombok.Builder;
import lombok.Value;
import org.jobrunr.jobs.context.JobContext;

@Builder
@Value
public class ExportJobFiles implements JobContext.Metadata {
  String job_id;
  String resource_type;
  String file_id;
}
