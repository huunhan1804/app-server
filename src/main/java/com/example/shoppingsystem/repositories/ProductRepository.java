package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Category;
import com.example.shoppingsystem.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_CategoryId(Long categoryId);

    @Modifying
    @Query(value = "SELECT * FROM product ORDER BY SOLD_AMOUNT DESC LIMIT 10", nativeQuery = true)
    List<Product> findListBestSellerProduct();
    List<Product> findByCategoryAndProductIdNot(Category productCategory, long productId);

    List<Product> findByProductNameContainingIgnoreCase(String keyword);
}
