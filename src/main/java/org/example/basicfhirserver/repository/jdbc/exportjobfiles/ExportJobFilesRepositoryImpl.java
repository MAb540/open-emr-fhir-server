package org.example.basicfhirserver.repository.jdbc.exportjobfiles;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toLocalDateTime;
import static org.example.basicfhirserver.repository.jdbc.utils.DBUtils.toUuid;

@Repository
public class ExportJobFilesRepositoryImpl implements ExportJobFilesRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ExportJobFilesRepositoryImpl(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<ExportJobFilesDBRecord> find() {

        StringBuilder sql = exportJobFilesQuery();
        MapSqlParameterSource params = new MapSqlParameterSource();

        return namedParameterJdbcTemplate.query(sql.toString(), params, exportJobFilesDBRecordRowMapper());
    }


    private StringBuilder exportJobFilesQuery() {
        return new StringBuilder("""
                SELECT   
                    je.job_id, 
                    je.resource_type, 
                    je.file_id, 
                    je.created_at 
                from  
                    jobrunr_export_job_files je
                """);
    }

    private RowMapper<ExportJobFilesDBRecord> exportJobFilesDBRecordRowMapper() {
        return (rs, rowNum) -> ExportJobFilesDBRecord.builder()
                .jobUuid(toUuid(rs.getBytes("job_id")))
                .resourceType(rs.getString("resource_type"))
                .fileId(rs.getString("file_id"))
                .createdAt(toLocalDateTime(rs.getTimestamp("created_at")))
                .build();
    }

}
