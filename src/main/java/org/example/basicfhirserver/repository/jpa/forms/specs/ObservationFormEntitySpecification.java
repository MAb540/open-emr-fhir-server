package org.example.basicfhirserver.repository.jpa.forms.specs;

import jakarta.persistence.criteria.Predicate;
import org.example.basicfhirserver.domain.entities.FormsEntity;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObservationFormEntitySpecification {

    public static Specification<FormsEntity> from(
            ObservationSearchQuery query) {

        return (root, cq, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(
                    cb.equal(root.get("formdir"), "vitals")
            );

            if (query.getPatientId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("pid"),
                                Long.valueOf(query.getPatientId())
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }

}
