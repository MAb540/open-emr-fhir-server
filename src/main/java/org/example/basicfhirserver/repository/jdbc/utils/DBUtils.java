package org.example.basicfhirserver.repository.jdbc.utils;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.sql.Date;

public class DBUtils {
    public static LocalDateTime toLocalDateTime(
            Timestamp timestamp
    ) {
        return timestamp == null
                ? null
                : timestamp.toLocalDateTime();
    }

    public static LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    public static byte[] toBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID toUuid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

}
