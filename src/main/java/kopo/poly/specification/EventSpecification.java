package kopo.poly.specification;

import kopo.poly.repository.entity.EventEntity;
import org.springframework.data.jpa.domain.Specification;

public class EventSpecification {

    public static Specification<EventEntity> eventPlace(String eventPlace) {
        return (root, query, criteriaBuilder) -> {
            if (eventPlace == null || eventPlace.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("eventPlace"), eventPlace);
        };
    }

    public static Specification<EventEntity> eventSort(String eventSort) {
        return (root, query, criteriaBuilder) -> {
            if (eventSort == null || eventSort.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("eventSort"), eventSort);
        };
    }

    public static Specification<EventEntity> eventDate(String eventDate) {
        return (root, query, criteriaBuilder) -> {
            if (eventDate == null || eventDate.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("eventDate"), eventDate);
        };
    }
}
