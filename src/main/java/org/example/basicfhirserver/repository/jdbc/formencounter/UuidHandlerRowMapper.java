package org.example.basicfhirserver.repository.jdbc.formencounter;


import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import java.beans.PropertyEditorSupport;
import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidHandlerRowMapper<T> extends BeanPropertyRowMapper<T> {

    public UuidHandlerRowMapper(Class<T> mappedClass) {
        super(mappedClass);
    }

    @Override
    protected void initBeanWrapper(BeanWrapper bw) {
        super.initBeanWrapper(bw);

        bw.registerCustomEditor(UUID.class, new PropertyEditorSupport() {
            @Override
            public void setValue(Object value) {
                if (value instanceof byte[] bytes) {
                    super.setValue(toUuid(bytes));
                } else {
                    super.setValue(value);
                }
            }
        });
    }

    private UUID toUuid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

}
