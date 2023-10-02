package com.example.shoppingsystem.filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FilterService {
    <T> Specification<T> createSpecification(FilterCriteria criteria);
    <T> Specification<T> buildSpecification(List<FilterCriteria> filterCriteriaList, FilterOperator operator);
    List<FilterCriteria> buildFilterCriteriaList(List<String> filterBy, List<String> filterValue, List<String> filterOperation);
     Pageable createPageable(int page, int size, String sortBy, String sortOrder) ;
    FilterOperator getFilterOperator(String filterOperator);
}
