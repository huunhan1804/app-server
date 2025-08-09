package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    interface CategoryStatView {
        String getCategoryName();
        Long getProductCount();
        BigDecimal getTotalSales();
    }

    @Query("""
        SELECT c.categoryName AS categoryName,
               COUNT(DISTINCT p.productId) AS productCount,
               COALESCE(SUM(od.subtotal), 0) AS totalSales
        FROM Category c
        LEFT JOIN Product p ON p.category.categoryId = c.categoryId
        LEFT JOIN OrderDetail od ON od.product.productId = p.productId
        GROUP BY c.categoryId, c.categoryName
        ORDER BY totalSales DESC
    """)
    List<CategoryStatView> getCategoryStats();
}

