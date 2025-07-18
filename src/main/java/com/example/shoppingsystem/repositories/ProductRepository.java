package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.ApprovalStatus;
import com.example.shoppingsystem.entities.Category;
import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory_CategoryId(Long categoryId);

    @Query(value = "SELECT * FROM product ORDER BY SOLD_AMOUNT DESC LIMIT 10", nativeQuery = true)
    List<Product> findListBestSellerProduct();

    List<Product> findByCategoryAndProductIdNot(Category productCategory, long productId);

    List<Product> findByProductNameContainingIgnoreCase(String keyword);

    List<Product> findByApprovalStatus_StatusCode(String statusCode);

    List<Product> findProductByAccountAndProductId(Account account, long productId);

    List<Product> findByAccount(Account account);

    List<Product> findByApprovalStatus(ApprovalStatus status);

    // Sửa lỗi: Thêm query cho status entity
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    Long countByApprovalStatus(@Param("status") ApprovalStatus status);

    // Thêm query cho ProductStatus enum
    @Query("SELECT COUNT(p) FROM Product p WHERE p.productStatus = :status")
    Long countByProductStatus(@Param("status") ProductStatus status);

    @Query("SELECT NEW map(c.categoryName as categoryName, COUNT(p) as productCount) " +
            "FROM Product p JOIN p.category c GROUP BY c.categoryId, c.categoryName")
    List<Map<String, Object>> getCategoryDistribution();
}