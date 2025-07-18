package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByParentCategory_ParentCategoryId(Long parentCategoryId);
 //   Category findByName(String name);
    Category findByCategoryId(Long categoryId);
}
