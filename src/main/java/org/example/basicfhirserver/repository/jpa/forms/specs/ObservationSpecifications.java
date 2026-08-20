package org.example.basicfhirserver.repository.jpa.forms.specs;

import org.example.basicfhirserver.domain.entities.FormVitalsEntity;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ObservationSpecifications {

    public static Specification<FormVitalsEntity> from(
            ObservationSearchQuery query) {

        return (root, cq, cb) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            if (query.getPatientId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("pid"),
                                Long.valueOf(query.getPatientId())
                        )
                );
            }

            if (query.getDate() != null) {
                SearchValue<LocalDateTime> date = query.getDate();

                if (date.getPrefix() == null) {
                    predicates.add(
                            cb.equal(
                                    root.get("date"),
                                    date.getValue()
                            )
                    );
                } else {
                    switch (date.getPrefix()) {
                        case NOT_EQUAL:
                            predicates.add(
                                    cb.notEqual(
                                            root.get("date"),
                                            date.getValue()
                                    )
                            );
                            break;

                        case LESSTHAN:
                            predicates.add(
                                    cb.lessThan(
                                            root.get("date"),
                                            date.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN:
                            predicates.add(
                                    cb.greaterThan(
                                            root.get("date"),
                                            date.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.greaterThanOrEqualTo(
                                            root.get("date"),
                                            date.getValue()
                                    )
                            );
                            break;

                        case LESSTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.lessThanOrEqualTo(
                                            root.get("date"),
                                            date.getValue()
                                    )
                            );
                            break;

                        case EQUAL:
                        case APPROXIMATE:
                        case STARTS_AFTER:
                        case ENDS_BEFORE:
                        default:
                            predicates.add(
                                    cb.equal(
                                            root.get("date"),
                                            date.getValue()
                                    )
                            );
                    }
                }
            }

            if (query.getLastUpdated() != null) {
                SearchValue<LocalDateTime> lastUpdated = query.getLastUpdated();

                if (lastUpdated.getPrefix() == null) {
                    predicates.add(
                            cb.equal(
                                    root.get("lastUpdated"),
                                    lastUpdated.getValue()
                            )
                    );
                } else {
                    switch (lastUpdated.getPrefix()) {
                        case NOT_EQUAL:
                            predicates.add(
                                    cb.notEqual(
                                            root.get("lastUpdated"),
                                            lastUpdated.getValue()
                                    )
                            );
                            break;

                        case LESSTHAN:
                            predicates.add(
                                    cb.lessThan(
                                            root.get("lastUpdated"),
                                            lastUpdated.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN:
                            predicates.add(
                                    cb.greaterThan(
                                            root.get("lastUpdated"),
                                            lastUpdated.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.greaterThanOrEqualTo(
                                            root.get("lastUpdated"),
                                            lastUpdated.getValue()
                                    )
                            );
                            break;

                        case LESSTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.lessThanOrEqualTo(
                                            root.get("lastUpdated"),
                                            lastUpdated.getValue()
                                    )
                            );
                            break;

                        case EQUAL:
                        case APPROXIMATE:
                        case STARTS_AFTER:
                        case ENDS_BEFORE:
                        default:
                            System.out.println("value " + lastUpdated.getValue());
                            predicates.add(
                                    cb.equal(
                                            root.get("lastUpdated"),
                                            lastUpdated.getValue()
                                    )
                            );
                    }
                }
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
