package com.electorama.msvc.citizenissues.model.specification;

import com.electorama.msvc.citizenissues.model.entity.IssueDetailEntity;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class IssueDetailSpecification {
  public static Specification<IssueDetailEntity> withFilters(
      Integer municipalityId,
      String userCreateId,
      Integer categoryId,
      Integer statusId,
      String search,
      Boolean isAdmin,
      Integer invoice,
      String isUrgent) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      root.join("issue", JoinType.INNER);
      if (municipalityId != null && municipalityId > 0) {
        predicates.add(
            criteriaBuilder.equal(root.get("issue").get("municipalityId"), municipalityId));
      }
      if (isUrgent.equals("true")) {
        predicates.add(criteriaBuilder.isTrue(root.get("urgent")));
      }
      if (isUrgent.equals("false")) {
        predicates.add(criteriaBuilder.isFalse(root.get("urgent")));
      }
      if (userCreateId != null && !userCreateId.equals("0")) {
        if (isAdmin) {
          predicates.add(criteriaBuilder.equal(root.get("userCreateId"), userCreateId));
        } else {
          predicates.add(criteriaBuilder.isNull(root.get("deletedDate")));
          Predicate userCreateIdPredicate =
              criteriaBuilder.or(
                  criteriaBuilder.isNull(root.get("userCreateId")),
                  criteriaBuilder.equal(root.get("userCreateId"), userCreateId));
          predicates.add(userCreateIdPredicate);
        }
      }
      if (categoryId != null && categoryId > 0) {
        predicates.add(criteriaBuilder.equal(root.get("issueCategory"), categoryId));
      }
      if (invoice != null && invoice > 0) {
        predicates.add(
            criteriaBuilder.like(
                root.get("issue").get("invoice"), "%" + invoice.toString().toLowerCase()));
      }
      if (statusId != null && statusId > 0) {
        predicates.add(criteriaBuilder.equal(root.get("issueStatus"), statusId));
      }
      if (search != null && !search.equals("")) {
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("issue").get("fullName")),
                    "%" + search.toLowerCase() + "%"),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("issue").get("phone")),
                    "%" + search.toLowerCase() + "%")));
      }
      query.orderBy(criteriaBuilder.desc(root.get("registrationDate")));

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
