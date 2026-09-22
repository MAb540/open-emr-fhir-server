package org.example.basicfhirserver.repository.jpa.patient.specs;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PatientSpecifications {

    public static Specification<LegacyPatientEntity> from(
            PatientSearchQuery query) {

        return (root, cq, cb) -> {

            List<Predicate> predicates = new ArrayList<>();


            if (query.getPatientId() != null && !query.getPatientId().isEmpty()) {
//                predicates.add(cb.equal(root.get("uuid"), UUID.fromString(query.getPatientId())));
                CriteriaBuilder.In<UUID> inClause = cb.in(root.get("uuid"));
                query.getPatientId().stream().map(UUID::fromString)
                                .forEach(inClause::value);
                predicates.add(inClause);
            }

            if (query.getIdentifier() != null) {
                predicates.add(cb.equal(root.get("ss"), query.getIdentifier()));
            }

            if (query.getFirstName() != null) {
                if (query.getFirstName().isContains()) {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get("fname")),
                                    "%" + query.getFirstName().getValue().toLowerCase() + "%"
                            )
                    );
                } else if (query.getFirstName().isExact()) {
                    predicates.add(
                            cb.equal(root.get("fname"),
                                    query.getFirstName().getValue())
                    );
                } else {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get("fname")),
                                    query.getFirstName().getValue().toLowerCase() + "%"
                            )
                    );
                }
            }

            if (query.getLastName() != null) {
                if (query.getLastName().isContains()) {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get("lname")),
                                    "%" + query.getLastName().getValue().toLowerCase() + "%"
                            )
                    );
                } else if (query.getLastName().isExact()) {
                    predicates.add(
                            cb.equal(root.get("lname"),
                                    query.getLastName().getValue())
                    );
                } else {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get("lname")),
                                    query.getLastName().getValue().toLowerCase() + "%"
                            )
                    );
                }
            }

            if (query.getName() != null) {
                if (query.getName().isContains()) {
                    predicates.add(
                            cb.or(
                                    cb.like(
                                            cb.lower(root.get("lname")),
                                            "%" + query.getName().getValue().toLowerCase() + "%"
                                    ),
                                    cb.like(
                                            cb.lower(root.get("fname")),
                                            "%" + query.getName().getValue().toLowerCase() + "%"
                                    )
                            )
                    );
                } else if (query.getName().isExact()) {
                    predicates.add(
                            cb.or(
                                    cb.equal(root.get("fname"),
                                            query.getName()),
                                    cb.equal(root.get("lname"),
                                            query.getName())
                            )
                    );
                } else {
                    predicates.add(
                            cb.or(
                                    cb.like(
                                            cb.lower(root.get("lname")),
                                            query.getName().getValue().toLowerCase() + "%"
                                    ),
                                    cb.like(
                                            cb.lower(root.get("fname")),
                                            query.getName().getValue().toLowerCase() + "%"
                                    )
                            )
                    );
                }
            }

            if (query.getBirthDate() != null) {
                SearchValue<LocalDate> birthDate = query.getBirthDate();
                if (birthDate.getPrefix() == null) {
                    predicates.add(
                            cb.equal(
                                    root.get("dob"),
                                    birthDate.getValue()
                            )
                    );
                } else {
                    switch (birthDate.getPrefix()) {
                        case NOT_EQUAL:
                            predicates.add(
                                    cb.notEqual(
                                            root.get("dob"),
                                            birthDate.getValue()
                                    )
                            );
                            break;

                        case LESSTHAN:
                            predicates.add(
                                    cb.lessThan(
                                            root.get("dob"),
                                            birthDate.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN:
                            predicates.add(
                                    cb.greaterThan(
                                            root.get("dob"),
                                            birthDate.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.greaterThanOrEqualTo(
                                            root.get("dob"),
                                            birthDate.getValue()
                                    )
                            );
                            break;


                        case LESSTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.lessThanOrEqualTo(
                                            root.get("dob"),
                                            birthDate.getValue()
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
                                            root.get("dob"),
                                            birthDate.getValue()
                                    )
                            );
                    }
                }
            }

            if (query.getDeathDate() != null) {
                SearchValue<LocalDateTime> deathDate = query.getDeathDate();
                if (deathDate.getPrefix() == null) {
                    predicates.add(
                            cb.equal(
                                    root.get("deceasedDate"),
                                    deathDate.getValue()
                            )
                    );
                } else {
                    switch (deathDate.getPrefix()) {
                        case NOT_EQUAL:
                            predicates.add(
                                    cb.notEqual(
                                            root.get("deceasedDate"),
                                            deathDate.getValue()
                                    )
                            );
                            break;

                        case LESSTHAN:
                            predicates.add(
                                    cb.lessThan(
                                            root.get("deceasedDate"),
                                            deathDate.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN:
                            predicates.add(
                                    cb.greaterThan(
                                            root.get("deceasedDate"),
                                            deathDate.getValue()
                                    )
                            );
                            break;

                        case GREATERTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.greaterThanOrEqualTo(
                                            root.get("deceasedDate"),
                                            deathDate.getValue()
                                    )
                            );
                            break;


                        case LESSTHAN_OR_EQUALS:
                            predicates.add(
                                    cb.lessThanOrEqualTo(
                                            root.get("deceasedDate"),
                                            deathDate.getValue()
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
                                            root.get("deceasedDate"),
                                            deathDate.getValue()
                                    )
                            );
                    }
                }
            }

            return cb.and(
                    predicates.toArray(new Predicate[0]));
        };
    }

}
