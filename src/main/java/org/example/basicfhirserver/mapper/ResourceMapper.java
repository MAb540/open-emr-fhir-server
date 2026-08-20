package org.example.basicfhirserver.mapper;


public interface ResourceMapper<T, K> {
    T toR4(K source);
}