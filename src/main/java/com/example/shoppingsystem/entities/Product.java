package com.example.shoppingsystem.entities;

import com.example.shoppingsystem.enums.ApprovalStatusEnum;
import com.example.shoppingsystem.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "CATEGORY_ID", referencedColumnName = "CATEGORY_ID", nullable = false)
    private Category category;

    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name = "PRODUCT_DESCRIPTION", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "LIST_PRICE", precision = 10, scale = 2, nullable = false)
    private BigDecimal listPrice;

    @Column(name = "SALE_PRICE", precision = 10, scale = 2, nullable = false)
    private BigDecimal salePrice;

    @Column(name = "INVENTORY_QUANTITY", nullable = false)
    private Integer inventoryQuantity;

    @Column(name = "DESIRED_QUANTITY", nullable = false)
    private Integer desiredQuantity;

    @Column(name = "SOLD_AMOUNT")
    private Integer soldAmount;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductVariant> productVariants;

    @OneToOne
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ACCOUNT_ID", nullable = false)
    private Account account;

    @Column(name = "IS_SALE")
    private Boolean isSale;

    @ManyToOne
    @JoinColumn(name = "STATUS_ID", referencedColumnName = "STATUS_ID", nullable = false)
    private ApprovalStatus status; // ✅ Tên đã sửa

    @Enumerated(EnumType.STRING)
    @Column(name = "PRODUCT_STATUS")
    private ProductStatus productStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "APPROVAL_STATUS")
    private ApprovalStatusEnum approvalStatus; // ✅ Enum trạng thái duyệt
}
