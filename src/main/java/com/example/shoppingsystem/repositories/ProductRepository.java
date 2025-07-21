package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.Category;
import com.example.shoppingsystem.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategory_CategoryId(Long categoryId);

    @Modifying
    @Query(value = "SELECT * FROM product ORDER BY SOLD_AMOUNT DESC LIMIT 10", nativeQuery = true)
    List<Product> findListBestSellerProduct();
    List<Product> findByCategoryAndProductIdNot(Category productCategory, long productId);

    List<Product> findByProductNameContainingIgnoreCase(String keyword);
    List<Product> findByApprovalStatus_StatusCode(String statusCode);
    List<Product> findProductByAccountAndProductId(Account account, long productId);
    List<Product> findByAccount(Account account);

    Page<Product> findByApprovalStatus_StatusCode(String statusCode, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:status IS NULL OR p.approvalStatus.statusCode = :status) AND " +
            "(:category IS NULL OR p.category.categoryName = :category) AND " +
            "(:agency IS NULL OR p.account.fullname = :agency) AND " +
            "(:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findProductsWithFilters(
            @Param("status") String status,
            @Param("category") String category,
            @Param("agency") String agency,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
