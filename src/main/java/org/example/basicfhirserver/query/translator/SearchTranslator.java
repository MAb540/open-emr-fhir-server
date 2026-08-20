package org.example.basicfhirserver.query.translator;

public interface SearchTranslator<T, V> {

    T translate(V criteria);

}
