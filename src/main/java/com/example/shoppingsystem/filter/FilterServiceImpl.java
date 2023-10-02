package com.example.shoppingsystem.filter;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilterServiceImpl implements FilterService {

    @Override
    public <T> Specification<T> createSpecification(FilterCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            if (criteria.getKey().contains(".")) {
                String[] joinAttributes = criteria.getKey().split("\\.");
                Join<?, ?> join = null;
                for (int i = 0; i < joinAttributes.length - 1; i++) {
                    if (join == null) {
                        join = root.join(joinAttributes[i]);
                    } else {
                        join = join.join(joinAttributes[i]);
                    }
                }
                Path<Object> fieldPath = join.get(joinAttributes[joinAttributes.length - 1]);
                return buildPredicate(criteria, criteriaBuilder, fieldPath);
            } else {
                Path<Object> fieldPath = root.get(criteria.getKey());
                return buildPredicate(criteria, criteriaBuilder, fieldPath);
            }
        };
    }

    private <T> Predicate buildPredicate(FilterCriteria criteria, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, Path<Object> fieldPath) {
        Object filterValue = criteria.getValue();

        switch (criteria.getOperation()) {
            case EQUALS:
                if (criteria.getKey().equals("createdBy") || criteria.getKey().equals("updatedBy")) {
                    return criteriaBuilder.equal(fieldPath, Long.valueOf(filterValue.toString()));
                } else if (criteria.getKey().equals("createdDate")) {
                    LocalDate createdDate = LocalDate.parse(filterValue.toString());
                    return criteriaBuilder.equal(fieldPath.as(LocalDate.class), createdDate);
                } else if (criteria.getKey().equals("updatedDate")) {
                    LocalDate updatedDate = LocalDate.parse(filterValue.toString());
                    return criteriaBuilder.equal(fieldPath.as(LocalDate.class), updatedDate);
                } else {
                    return criteriaBuilder.equal(fieldPath, filterValue.toString());
                }
            case CONTAINS:
                return criteriaBuilder.like(criteriaBuilder.lower(fieldPath.as(String.class)), "%" + filterValue.toString().toLowerCase() + "%");
            case GREATER_THAN:
                return criteriaBuilder.greaterThan(fieldPath.as(Comparable.class), (Comparable) filterValue);
            case LESS_THAN:
                return criteriaBuilder.lessThan(fieldPath.as(Comparable.class), (Comparable) filterValue);
            case DATE_IS:
                LocalDate dateIs = LocalDate.parse(filterValue.toString());
                return criteriaBuilder.equal(fieldPath.as(LocalDate.class), dateIs);
            case DATE_IS_NOT:
                LocalDate dateIsNot = LocalDate.parse(filterValue.toString());
                return criteriaBuilder.notEqual(fieldPath.as(LocalDate.class), dateIsNot);
            case DATE_BEFORE:
                LocalDate dateBefore = LocalDate.parse(filterValue.toString());
                return criteriaBuilder.lessThan(fieldPath.as(LocalDate.class), dateBefore);
            case DATE_AFTER:
                LocalDate dateAfter = LocalDate.parse(filterValue.toString());
                return criteriaBuilder.greaterThan(fieldPath.as(LocalDate.class), dateAfter);
            case STARTS_WITH:
                return criteriaBuilder.like(fieldPath.as(String.class), filterValue.toString() + "%");
            case NOT_CONTAINS:
                return criteriaBuilder.notLike(criteriaBuilder.lower(fieldPath.as(String.class)), "%" + filterValue.toString().toLowerCase() + "%");
            case ENDS_WITH:
                return criteriaBuilder.like(fieldPath.as(String.class), "%" + filterValue.toString());
            case NOT_EQUALS:
                return criteriaBuilder.notEqual(fieldPath, filterValue.toString());
            default:
                throw new IllegalArgumentException("Unsupported filter operation: " + criteria.getOperation());
        }
    }

    @Override
    public <T> Specification<T> buildSpecification(List<FilterCriteria> filterCriteriaList, FilterOperator operator) {
        Specification<T> specification = Specification.where(null);

        for (FilterCriteria criteria : filterCriteriaList) {
            Specification<T> criteriaSpecification = createSpecification(criteria);
            specification = operator == FilterOperator.AND
                    ? specification.and(criteriaSpecification)
                    : specification.or(criteriaSpecification);
        }

        return specification;
    }

    @Override
    public List<FilterCriteria> buildFilterCriteriaList(List<String> filterBy, List<String> filterValue, List<String> filterOperation) {
        List<FilterCriteria> filterCriteriaList = new ArrayList<>();
        if (filterBy != null && filterValue != null && filterBy.size() == filterValue.size() && filterBy.size() == filterOperation.size()) {
            for (int i = 0; i < filterBy.size(); i++) {
                FilterCriteria criteria = new FilterCriteria();
                criteria.setKey(filterBy.get(i));
                criteria.setValue(filterValue.get(i));
                String operation = filterOperation.get(i);
                switch (operation.toUpperCase()) {
                    case "EQUALS":
                        criteria.setOperation(FilterOperation.EQUALS);
                        break;
                    case "CONTAINS":
                    case "LIKE":
                        criteria.setOperation(FilterOperation.CONTAINS);
                        break;
                    case "GREATERTHAN":
                        criteria.setOperation(FilterOperation.GREATER_THAN);
                        break;
                    case "LESSTHAN":
                        criteria.setOperation(FilterOperation.LESS_THAN);
                        break;
                    case "DATEIS":
                        criteria.setOperation(FilterOperation.DATE_IS);
                        break;
                    case "DATEISNOT":
                        criteria.setOperation(FilterOperation.DATE_IS_NOT);
                        break;
                    case "DATEBEFORE":
                        criteria.setOperation(FilterOperation.DATE_BEFORE);
                        break;
                    case "DATEAFTER":
                        criteria.setOperation(FilterOperation.DATE_AFTER);
                        break;
                    case "STARTSWITH":
                        criteria.setOperation(FilterOperation.STARTS_WITH);
                        break;
                    case "NOTCONTAINS":
                        criteria.setOperation(FilterOperation.NOT_CONTAINS);
                        break;
                    case "ENDSWITH":
                        criteria.setOperation(FilterOperation.ENDS_WITH);
                        break;
                    case "NOTEQUALS":
                        criteria.setOperation(FilterOperation.NOT_EQUALS);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported filter operation: " + operation);
                }

                filterCriteriaList.add(criteria);
            }
        }
        return filterCriteriaList;
    }

    @Override
    public Pageable createPageable(int page, int size, String sortBy, String sortOrder) {
        Sort.Direction sortDirection = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, sortDirection, sortBy);
    }

    @Override
    public FilterOperator getFilterOperator(String filterOperator) {
        return filterOperator != null && filterOperator.equalsIgnoreCase("or")
                ? FilterOperator.OR : FilterOperator.AND;
    }
}
