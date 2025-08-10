package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.SupportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportCategoryRepository extends JpaRepository<SupportCategory, Long> {
    boolean existsBySupportCategoryName(String categoryName);
    List<SupportCategory> findAllByOrderBySupportCategoryNameAsc();
    Optional<SupportCategory> findBySupportCategoryNameAndSupportCategoryIdNot(
            String categoryName, Long categoryId);
}
