package com.example.shoppingsystem.utils;

import com.example.shoppingsystem.entities.*;
import com.example.shoppingsystem.enums.*;
import com.example.shoppingsystem.repositories.*;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SampleDataGenerator {

    private final AccountRepository accountRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final RoleRepository roleRepository;
    private final ApprovalStatusRepository approvalStatusRepository;
    private final ParentCategoryRepository parentCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final MultimediaRepository multimediaRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CouponRepository couponRepository;
    private final AccountCouponRepository accountCouponRepository;
    private final CouponOrderListRepository couponOrderListRepository;
    private final FeedbackRepository feedbackRepository;
    private final AddressRepository addressRepository;
    private final AgencyInfoRepository agencyInfoRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(new Locale("vi"));
    private final Random random = new Random();

    @Transactional
    public void generateSampleData() {
        System.out.println("=== BẮT ĐẦU TẠO DỮ LIỆU MẪU ===");

        try {
            // Xóa dữ liệu cũ trước (nếu có)
            clearExistingData();

            // 1. Tạo Roles
            List<Role> roles = createRoles();
            System.out.println("✓ Đã tạo " + roles.size() + " roles");

            // 2. Tạo Approval Status
            List<ApprovalStatus> approvalStatuses = createApprovalStatuses();
            System.out.println("✓ Đã tạo " + approvalStatuses.size() + " approval statuses");

            // 3. Tạo Parent Categories
            List<ParentCategory> parentCategories = createParentCategories();
            System.out.println("✓ Đã tạo " + parentCategories.size() + " parent categories");

            // 4. Tạo Categories
            List<Category> categories = createCategories(parentCategories);
            System.out.println("✓ Đã tạo " + categories.size() + " categories");

            // 5. Tạo Accounts
            List<Account> accounts = createAccounts(roles, approvalStatuses);
            System.out.println("✓ Đã tạo " + accounts.size() + " accounts");

            // 6. Tạo Memberships
            List<Membership> memberships = createMemberships(accounts);
            System.out.println("✓ Đã tạo " + memberships.size() + " memberships");

            // 7. Tạo Agency Info
            List<AgencyInfo> agencyInfos = createAgencyInfos(accounts, approvalStatuses);
            System.out.println("✓ Đã tạo " + agencyInfos.size() + " agency infos");

            // 8. Tạo Addresses
            List<Address> addresses = createAddresses(accounts);
            System.out.println("✓ Đã tạo " + addresses.size() + " addresses");

            // 9. Tạo Products
            List<Product> products = createProducts(categories, agencyInfos, approvalStatuses);
            System.out.println("✓ Đã tạo " + products.size() + " products");

            // 10. Tạo Product Variants
            List<ProductVariant> productVariants = createProductVariants(products);
            System.out.println("✓ Đã tạo " + productVariants.size() + " product variants");

            // 11. Tạo Multimedia
            List<Multimedia> multimedias = createMultimedia(accounts, products, productVariants, categories);
            System.out.println("✓ Đã tạo " + multimedias.size() + " multimedia");

            // 12. Tạo Carts
            List<Cart> carts = createCarts(accounts);
            System.out.println("✓ Đã tạo " + carts.size() + " carts");

            // 13. Tạo Cart Items
            List<CartItem> cartItems = createCartItems(carts, products, productVariants);
            System.out.println("✓ Đã tạo " + cartItems.size() + " cart items");

            // 14. Tạo Coupons
            List<Coupon> coupons = createCoupons(categories);
            System.out.println("✓ Đã tạo " + coupons.size() + " coupons");

            // 15. Tạo Account Coupons
            List<AccountCoupon> accountCoupons = createAccountCoupons(accounts, coupons);
            System.out.println("✓ Đã tạo " + accountCoupons.size() + " account coupons");

            // 16. Tạo Orders
            List<OrderList> orders = createOrders(accounts);
            System.out.println("✓ Đã tạo " + orders.size() + " orders");

            // 17. Tạo Order Details
            List<OrderDetail> orderDetails = createOrderDetails(orders, products, productVariants);
            System.out.println("✓ Đã tạo " + orderDetails.size() + " order details");

            // 18. Tạo Coupon Order List
            List<CouponOrderList> couponOrderLists = createCouponOrderLists(coupons, orders);
            System.out.println("✓ Đã tạo " + couponOrderLists.size() + " coupon order lists");

            // 19. Tạo Feedbacks
            List<Feedback> feedbacks = createFeedbacks(accounts, products);
            System.out.println("✓ Đã tạo " + feedbacks.size() + " feedbacks");

            // Tính tổng
            long total = roleRepository.count() + approvalStatusRepository.count() +
                    parentCategoryRepository.count() + categoryRepository.count() +
                    accountRepository.count() + membershipRepository.count() +
                    agencyInfoRepository.count() + addressRepository.count() +
                    productRepository.count() + productVariantRepository.count() +
                    multimediaRepository.count() + cartRepository.count() +
                    cartItemRepository.count() + couponRepository.count() +
                    accountCouponRepository.count() + orderRepository.count() +
                    orderDetailRepository.count() + couponOrderListRepository.count() +
                    feedbackRepository.count();

            System.out.println("=== HOÀN THÀNH TẠO DỮ LIỆU MẪU ===");
            System.out.println("TỔNG SỐ RECORDS: " + total);

        } catch (Exception e) {
            System.err.println("❌ LỖI KHI TẠO DỮ LIỆU: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo dữ liệu mẫu", e);
        }
    }

    private void clearExistingData() {
        try {
            System.out.println("🗑️  Xóa dữ liệu cũ...");

            // Xóa theo thứ tự ĐÚNG để tránh foreign key constraint
            feedbackRepository.deleteAllInBatch();
            couponOrderListRepository.deleteAllInBatch();
            orderDetailRepository.deleteAllInBatch();
            orderRepository.deleteAllInBatch();
            accountCouponRepository.deleteAllInBatch();
            couponRepository.deleteAllInBatch();
            cartItemRepository.deleteAllInBatch();
            cartRepository.deleteAllInBatch();
            multimediaRepository.deleteAllInBatch();
            productVariantRepository.deleteAllInBatch();
            productRepository.deleteAllInBatch();
            addressRepository.deleteAllInBatch();

            // QUAN TRỌNG: Xóa agency_info TRƯỚC account
            agencyInfoRepository.deleteAllInBatch();
            membershipRepository.deleteAllInBatch();

            accessTokenRepository.deleteAllInBatch();
            refreshTokenRepository.deleteAllInBatch();
            accountRepository.deleteAllInBatch();  // Xóa account SAU agency_info

            categoryRepository.deleteAllInBatch();
            parentCategoryRepository.deleteAllInBatch();
            approvalStatusRepository.deleteAllInBatch();
            roleRepository.deleteAllInBatch();


            System.out.println("✓ Đã xóa dữ liệu cũ");

        } catch (Exception e) {
            System.out.println("⚠️  Lỗi khi xóa dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Role> createRoles() {
        System.out.println("Tạo roles...");

        // Kiểm tra xem đã có ADMIN role chưa
        Optional<Role> adminRole = Optional.ofNullable(roleRepository.findByRoleCode("ADMIN"));
        if (adminRole.isPresent()) {
            System.out.println("Roles đã tồn tại, lấy từ database");
            return roleRepository.findAll();
        }

        List<Role> roles = Arrays.asList(
                Role.builder()
                        .roleCode("admin")
                        .roleName("Administrator")
                        .description("System Administrator")
                        .build(),
                Role.builder()
                        .roleCode("customer")
                        .roleName("customer")
                        .description("Regular Customer")
                        .build(),
                Role.builder()
                        .roleCode("agency")
                        .roleName("agency")
                        .description("Product Agency/Seller")
                        .build(),
                Role.builder()
                        .roleCode("MODERATOR")
                        .roleName("Moderator")
                        .description("Content Moderator")
                        .build(),
                Role.builder()
                        .roleCode("SUPPORT")
                        .roleName("Support")
                        .description("Customer Support")
                        .build()
        );

        List<Role> savedRoles = roleRepository.saveAll(roles);
        System.out.println("Saved " + savedRoles.size() + " roles");
        return savedRoles;
    }

    private List<ApprovalStatus> createApprovalStatuses() {
        List<ApprovalStatus> statuses = Arrays.asList(
                ApprovalStatus.builder()
                        .statusCode("PENDING")
                        .statusName("Đang chờ duyệt")
                        .build(),
                ApprovalStatus.builder()
                        .statusCode("APPROVED")
                        .statusName("Đã duyệt")
                        .build(),
                ApprovalStatus.builder()
                        .statusCode("REJECTED")
                        .statusName("Từ chối")
                        .build(),
                ApprovalStatus.builder()
                        .statusCode("SUSPENDED")
                        .statusName("Tạm ngưng")
                        .build(),
                ApprovalStatus.builder()
                        .statusCode("UNDER_REVIEW")
                        .statusName("Đang xem xét")
                        .build()
        );
        return approvalStatusRepository.saveAll(statuses);
    }

    private List<ParentCategory> createParentCategories() {
        List<ParentCategory> parentCategories = new ArrayList<>();
        String[] categoryNames = {
                "Điện tử", "Thời trang", "Nhà cửa & Đời sống", "Sức khỏe & Làm đẹp",
                "Thể thao & Du lịch", "Ô tô & Xe máy", "Mẹ & Bé", "Sách & Văn phòng phẩm",
                "Thực phẩm & Đồ uống", "Giải trí & Sở thích", "Thiết bị số", "Máy tính & Laptop",
                "Điện gia dụng", "Đồ chơi", "Nông nghiệp", "Công nghiệp", "Y tế & Dược phẩm",
                "Giáo dục & Đào tạo", "Dịch vụ", "Khác"
        };

        for (String name : categoryNames) {
            parentCategories.add(ParentCategory.builder()
                    .parentCategoryName(name)
                    .parentCategoryDescription(faker.lorem().sentence(10))
                    .build());
        }
        return parentCategoryRepository.saveAll(parentCategories);
    }

    private List<Category> createCategories(List<ParentCategory> parentCategories) {
        List<Category> categories = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            ParentCategory parentCategory = parentCategories.get(random.nextInt(parentCategories.size()));
            categories.add(Category.builder()
                    .parentCategory(parentCategory)
                    .categoryName(faker.commerce().department() + " " + (i + 1))
                    .categoryDescription(faker.lorem().sentence(8))
                    .build());
        }
        return categoryRepository.saveAll(categories);
    }

    private List<Account> createAccounts(List<Role> roles, List<ApprovalStatus> approvalStatuses) {
        List<Account> accounts = new ArrayList<>();

        // Lấy roles
        Role adminRole = roles.stream().filter(r -> r.getRoleCode().equals("ADMIN")).findFirst().orElse(roles.get(0));
        Role customerRole = roles.stream().filter(r -> r.getRoleCode().equals("customer")).findFirst().orElse(roles.get(1));
        Role agencyRole = roles.stream().filter(r -> r.getRoleCode().equals("agency")).findFirst().orElse(roles.get(2));

        // Admin accounts (10)
        for (int i = 0; i < 10; i++) {
            accounts.add(createAccount(adminRole, approvalStatuses, i, "admin"));
        }

        // Customer accounts (700)
        for (int i = 0; i < 700; i++) {
            accounts.add(createAccount(customerRole, approvalStatuses, i + 10, "customer"));
        }

        // Agency accounts (250)
        for (int i = 0; i < 250; i++) {
            accounts.add(createAccount(agencyRole, approvalStatuses, i + 710, "agency"));
        }

        return accountRepository.saveAll(accounts);
    }

    private Account createAccount(Role role, List<ApprovalStatus> approvalStatuses, int index, String prefix) {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String username = prefix + "_" + (firstName + lastName + index).toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-zA-Z0-9]", "");

        return Account.builder()
                .username(username)
                .email(username + "@example.com")
                .phone("09" + String.format("%8d", random.nextInt(100000000)))
                .password(passwordEncoder.encode("password123"))
                .fullname(firstName + " " + lastName)
                .birthdate(faker.date().birthday(18, 65))
                .gender(random.nextBoolean() ? "Nam" : "Nữ")
                .isBanned(random.nextDouble() < 0.05)
                .lastLogin(LocalDateTime.now().minusDays(random.nextInt(30)))
                .accountStatus(Account.AccountStatus.ACTIVE)
                .role(role)
                .approvalStatus(approvalStatuses.get(1)) // APPROVED
                .build();
    }

    private List<Membership> createMemberships(List<Account> accounts) {
        List<Membership> memberships = new ArrayList<>();

        accounts.stream()
                .filter(account -> "customer".equals(account.getRole().getRoleCode()))
                .forEach(customer -> {
                    memberships.add(Membership.builder()
                            .account(customer)
                            .membershipLevel(Membership.MembershipLevel.values()[random.nextInt(Membership.MembershipLevel.values().length)])
                            .totalSpent(BigDecimal.valueOf(random.nextDouble() * 50000000))
                            .totalOrders(random.nextInt(100))
                            .pointsEarned(random.nextInt(10000))
                            .pointsUsed(random.nextInt(5000))
                            .pointsBalance(random.nextInt(5000))
                            .levelUpDate(LocalDateTime.now().minusDays(random.nextInt(365)))
                            .nextLevelRequirement(BigDecimal.valueOf(random.nextDouble() * 10000000))
                            .discountPercentage(BigDecimal.valueOf(random.nextDouble() * 20))
                            .freeShipping(random.nextBoolean())
                            .prioritySupport(random.nextBoolean())
                            .earlyAccess(random.nextBoolean())
                            .build());
                });

        return membershipRepository.saveAll(memberships);
    }

    private List<AgencyInfo> createAgencyInfos(List<Account> accounts, List<ApprovalStatus> approvalStatuses) {
        List<AgencyInfo> agencyInfos = new ArrayList<>();

        accounts.stream()
                .filter(account -> "agency".equals(account.getRole().getRoleCode()))
                .forEach(agency -> {
                    agencyInfos.add(AgencyInfo.builder()
                            .account(agency)
                            .shopName(faker.company().name())
                            .shopAddressDetail(faker.address().fullAddress())
                            .shopEmail(agency.getEmail())
                            .shopPhone(faker.phoneNumber().cellPhone())
                            .taxNumber(String.valueOf(1000000000L + random.nextInt(900000000)))
                            .fullNameApplicant(agency.getFullname())
                            .birthdateApplicant(agency.getBirthdate())
                            .genderApplicant(agency.getGender())
                            .idCardNumber(String.valueOf(100000000000L + random.nextInt(900000000)))
                            .dateOfIssueIdCard(faker.date().past(365 * 10, TimeUnit.DAYS))
                            .placeOfIssueIdCard(faker.address().city())
                            .idCardFrontImageUrl("https://example.com/id-front-" + agency.getAccountId() + ".jpg")
                            .idCardBackImageUrl("https://example.com/id-back-" + agency.getAccountId() + ".jpg")
                            // FIX: Thay đổi thành JSON valid
                            .businessLicenseUrls("[\"https://example.com/business-" + agency.getAccountId() + ".jpg\"]")
                            .professionalCertUrls("[\"https://example.com/cert-" + agency.getAccountId() + ".jpg\"]")
                            .diplomaCertUrls("[\"https://example.com/diploma-" + agency.getAccountId() + ".jpg\"]")
                            .approvalStatus(approvalStatuses.get(1)) // APPROVED
                            .submittedDate(faker.date().past(180, TimeUnit.DAYS))
                            .build());
                });

        return agencyInfoRepository.saveAll(agencyInfos);
    }
    private List<Address> createAddresses(List<Account> accounts) {
        List<Address> addresses = new ArrayList<>();

        for (Account account : accounts) {
            int numAddresses = random.nextInt(3) + 1;
            for (int i = 0; i < numAddresses; i++) {
                addresses.add(Address.builder()
                        .account(account)
                        .fullName(account.getFullname())
                        .phone("09" + String.format("%8d", random.nextInt(100000000)))
                        .addressDetail(faker.address().fullAddress())
                        .isDefault(i == 0)
                        .build());
            }
        }

        return addressRepository.saveAll(addresses);
    }

    private List<Product> createProducts(List<Category> categories, List<AgencyInfo> agencies, List<ApprovalStatus> approvalStatuses) {
        List<Product> products = new ArrayList<>();

        if (agencies.isEmpty()) {
            System.out.println("⚠️  Không có agency nào để tạo product!");
            return products;
        }

        for (int i = 0; i < 2000; i++) {
            Category category = categories.get(random.nextInt(categories.size()));
            AgencyInfo agency = agencies.get(random.nextInt(agencies.size()));

            BigDecimal listPrice = BigDecimal.valueOf(50000 + random.nextInt(9950000));
            BigDecimal salePrice = listPrice.multiply(BigDecimal.valueOf(0.7 + random.nextDouble() * 0.3));

            products.add(Product.builder()
                    .category(category)
                    .productName(faker.commerce().productName() + " " + (i + 1))
                    .productDescription(faker.lorem().paragraph(5))
                    .listPrice(listPrice)
                    .salePrice(salePrice)
                    .inventoryQuantity(random.nextInt(1000) + 10)
                    .desiredQuantity(random.nextInt(500) + 50)
                    .soldAmount(random.nextInt(200))
                    .agencyInfo(agency)
                    .isSale(random.nextBoolean())
                    .approvalStatus(approvalStatuses.get(1)) // APPROVED
                    .build());
        }

        return productRepository.saveAll(products);
    }

    private List<ProductVariant> createProductVariants(List<Product> products) {
        List<ProductVariant> productVariants = new ArrayList<>();

        for (Product product : products) {
            int numVariants = random.nextInt(3) + 1;
            for (int i = 0; i < numVariants; i++) {
                BigDecimal listPrice = product.getListPrice().add(BigDecimal.valueOf(random.nextInt(500000) - 250000));
                BigDecimal salePrice = listPrice.multiply(BigDecimal.valueOf(0.7 + random.nextDouble() * 0.3));

                productVariants.add(ProductVariant.builder()
                        .product(product)
                        .productVariantName(faker.color().name() + " - " + faker.commerce().material())
                        .listPrice(listPrice)
                        .salePrice(salePrice)
                        .inventoryQuantity(random.nextInt(500) + 10)
                        .desiredQuantity(random.nextInt(200) + 20)
                        .build());
            }
        }

        return productVariantRepository.saveAll(productVariants);
    }

    private List<Multimedia> createMultimedia(List<Account> accounts, List<Product> products,
                                              List<ProductVariant> productVariants, List<Category> categories) {
        List<Multimedia> multimedias = new ArrayList<>();

        // Account avatars
        accounts.forEach(account -> {
            multimedias.add(Multimedia.builder()
                    .account(account)
                    .multimediaUrl("https://picsum.photos/400/400?random=" + account.getAccountId())
                    .multimediaType(MultimediaType.IMAGE)
                    .build());
        });

        // Product images
        products.forEach(product -> {
            int numImages = random.nextInt(3) + 1;
            for (int i = 0; i < numImages; i++) {
                multimedias.add(Multimedia.builder()
                        .product(product)
                        .multimediaUrl("https://picsum.photos/500/500?random=" + (product.getProductId() * 10 + i))
                        .multimediaType(MultimediaType.IMAGE)
                        .build());
            }
        });

        // Product variant images (50% có ảnh riêng)
        productVariants.stream()
                .filter(variant -> random.nextBoolean())
                .forEach(variant -> {
                    multimedias.add(Multimedia.builder()
                            .productVariant(variant)
                            .multimediaUrl("https://picsum.photos/500/500?random=" + (variant.getProductVariantId() * 100))
                            .multimediaType(MultimediaType.IMAGE)
                            .build());
                });

        // Category images
        categories.forEach(category -> {
            multimedias.add(Multimedia.builder()
                    .category(category)
                    .multimediaUrl("https://picsum.photos/300/300?random=" + (category.getCategoryId() * 1000))
                    .multimediaType(MultimediaType.IMAGE)
                    .build());
        });

        return multimediaRepository.saveAll(multimedias);
    }

    private List<Cart> createCarts(List<Account> accounts) {
        List<Cart> carts = new ArrayList<>();

        accounts.forEach(account -> {
            carts.add(Cart.builder()
                    .account(account)
                    .totalItem(0) // Sẽ được cập nhật sau khi tạo cart items
                    .build());
        });

        return cartRepository.saveAll(carts);
    }

    private List<CartItem> createCartItems(List<Cart> carts, List<Product> products, List<ProductVariant> productVariants) {
        List<CartItem> cartItems = new ArrayList<>();

        for (Cart cart : carts) {
            int numItems = random.nextInt(5) + 1;
            for (int i = 0; i < numItems; i++) {
                Product product = products.get(random.nextInt(products.size()));
                ProductVariant variant = productVariants.stream()
                        .filter(pv -> pv.getProduct().getProductId().equals(product.getProductId()))
                        .findFirst()
                        .orElse(productVariants.get(random.nextInt(productVariants.size())));

                cartItems.add(CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .productVariant(variant)
                        .quantity(random.nextInt(5) + 1)
                        .build());
            }

            // Cập nhật totalItem cho cart
            cart.setTotalItem(numItems);
            cartRepository.save(cart);
        }

        return cartItemRepository.saveAll(cartItems);
    }

    private List<Coupon> createCoupons(List<Category> categories) {
        List<Coupon> coupons = new ArrayList<>();

        // Lấy danh sách agencies để gán cho coupon
        List<Account> agencies = accountRepository.findAll().stream()
                .filter(account -> "agency".equals(account.getRole().getRoleCode()))
                .toList();

        for (int i = 0; i < 200; i++) {
            Category category = random.nextBoolean() ?
                    categories.get(random.nextInt(categories.size())) : null;

            Account agency = random.nextBoolean() && !agencies.isEmpty() ?
                    agencies.get(random.nextInt(agencies.size())) : null;

            LocalDateTime startDate = LocalDateTime.now().minusDays(random.nextInt(30));
            LocalDateTime expiryDate = startDate.plusDays(30 + random.nextInt(335));

            coupons.add(Coupon.builder()
                    // THÊM CÁC TRƯỜNG BẮT BUỘC
                    .couponCode("COUPON" + String.format("%06d", i + 1))
                    .couponName("Coupon giảm giá " + (i + 1))
                    .category(category)
                    .agency(agency)
                    .discountValue(BigDecimal.valueOf(10000 + random.nextInt(490000)))
                    .discountType(DiscountType.values()[random.nextInt(DiscountType.values().length)])
                    .minPurchaseAmount(BigDecimal.valueOf(100000 + random.nextInt(900000)))
                    .maxDiscountAmount(BigDecimal.valueOf(500000 + random.nextInt(1000000)))
                    .usageLimit(100 + random.nextInt(900))
                    .usedCount(0)
                    .startDate(startDate)
                    .expiryDate(expiryDate)
                    .isActive(random.nextBoolean())
                    .couponType(CouponType.values()[random.nextInt(CouponType.values().length)])
                    .description(faker.lorem().sentence())
                    .termsConditions("Áp dụng cho đơn hàng trên " + (100000 + random.nextInt(900000)) + " VNĐ")
                    // CÁC TRƯỜNG CŨ
                    .isActivated(random.nextBoolean())
                    .remainingQuantity(10 + random.nextInt(990))
                    .minQuantity(1 + random.nextInt(4))
                    .maxQuantity(10 + random.nextInt(40))
                    .build());
        }

        return couponRepository.saveAll(coupons);
    }
    private List<AccountCoupon> createAccountCoupons(List<Account> accounts, List<Coupon> coupons) {
        List<AccountCoupon> accountCoupons = new ArrayList<>();

        for (int i = 0; i < 1500; i++) {
            Account account = accounts.get(random.nextInt(accounts.size()));
            Coupon coupon = coupons.get(random.nextInt(coupons.size()));

            accountCoupons.add(AccountCoupon.builder()
                    .account(account)
                    .coupon(coupon)
                    .build());
        }

        return accountCouponRepository.saveAll(accountCoupons);
    }

    private List<OrderList> createOrders(List<Account> accounts) {
        List<OrderList> orders = new ArrayList<>();
        List<Account> customers = accounts.stream()
                .filter(account -> "customer".equals(account.getRole().getRoleCode()))
                .toList();
        List<Account> agencies = accounts.stream()
                .filter(account -> "agency".equals(account.getRole().getRoleCode()))
                .toList();

        if (customers.isEmpty() || agencies.isEmpty()) {
            System.out.println("⚠️  Không có customer hoặc agency để tạo orders!");
            return orders;
        }

        for (int i = 0; i < 1500; i++) {
            Account customer = customers.get(random.nextInt(customers.size()));
            Account agency = agencies.get(random.nextInt(agencies.size()));

            orders.add(OrderList.builder()
                    .account(customer)
                    .agency(agency)
                    .orderDate(LocalDateTime.now().minusDays(random.nextInt(365)))
                    .totalPrice(BigDecimal.ZERO) // Sẽ được tính lại trong createOrderDetails
                    .orderStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)])
                    .addressDetail(faker.address().fullAddress())
                    .build());
        }

        return orderRepository.saveAll(orders);
    }
    private List<OrderDetail> createOrderDetails(List<OrderList> orders, List<Product> products, List<ProductVariant> productVariants) {
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderList order : orders) {
            int numItems = random.nextInt(3) + 1; // 1-3 items per order
            BigDecimal orderTotal = BigDecimal.ZERO;

            for (int i = 0; i < numItems; i++) {
                Product product = products.get(random.nextInt(products.size()));
                ProductVariant variant = productVariants.stream()
                        .filter(pv -> pv.getProduct().getProductId().equals(product.getProductId()))
                        .findFirst()
                        .orElse(productVariants.get(random.nextInt(productVariants.size())));

                int quantity = random.nextInt(3) + 1; // 1-3 quantity per item
                BigDecimal price = variant.getSalePrice();
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                orderTotal = orderTotal.add(subtotal);

                orderDetails.add(OrderDetail.builder()
                        .orderList(order)
                        .product(product)
                        .productVariant(variant)
                        .quantity(quantity)
                        .price(price)
                        .subtotal(subtotal)
                        .build());
            }

            // Cập nhật tổng tiền cho order - đảm bảo không quá lớn
            if (orderTotal.compareTo(BigDecimal.valueOf(10000000)) > 0) { // Nếu > 10 triệu
                orderTotal = BigDecimal.valueOf(1000000 + random.nextInt(2000000)); // Giới hạn 1-3 triệu
            }

            order.setTotalPrice(orderTotal);
            orderRepository.save(order);
        }

        return orderDetailRepository.saveAll(orderDetails);
    }
    private List<CouponOrderList> createCouponOrderLists(List<Coupon> coupons, List<OrderList> orders) {
        List<CouponOrderList> couponOrderLists = new ArrayList<>();

        for (int i = 0; i < 800; i++) {
            Coupon coupon = coupons.get(random.nextInt(coupons.size()));
            OrderList order = orders.get(random.nextInt(orders.size()));

            couponOrderLists.add(CouponOrderList.builder()
                    .coupon(coupon)
                    .orderList(order)
                    .build());
        }

        return couponOrderListRepository.saveAll(couponOrderLists);
    }

    private List<Feedback> createFeedbacks(List<Account> accounts, List<Product> products) {
        List<Feedback> feedbacks = new ArrayList<>();
        List<Account> customers = accounts.stream()
                .filter(account -> "customer".equals(account.getRole().getRoleCode()))
                .toList();

        if (customers.isEmpty() || products.isEmpty()) {
            System.out.println("⚠️  Không có customer hoặc product để tạo feedback!");
            return feedbacks;
        }

        for (int i = 0; i < 3000; i++) {
            Account customer = customers.get(random.nextInt(customers.size()));
            Product product = products.get(random.nextInt(products.size()));

            feedbacks.add(Feedback.builder()
                    .account(customer)
                    .product(product)
                    .rating(Rating.values()[random.nextInt(Rating.values().length)])
                    .comment(faker.lorem().paragraph(3))
                    .build());
        }

        return feedbackRepository.saveAll(feedbacks);
    }

    // Method để tạo dữ liệu theo batch để tránh lỗi memory
    @Transactional
    public void generateBigSampleData() {
        System.out.println("=== TẠO DỮ LIỆU MẪU LỚNHỌN (10K+ RECORDS) ===");

        try {
            clearExistingData();

            // Tạo dữ liệu cơ bản trước
            List<Role> roles = createRoles();
            List<ApprovalStatus> approvalStatuses = createApprovalStatuses();
            List<ParentCategory> parentCategories = createParentCategories();
            List<Category> categories = createCategoriesBig(parentCategories, 200); // 200 categories

            // Tạo accounts theo batch
            List<Account> allAccounts = new ArrayList<>();
            for (int batch = 0; batch < 10; batch++) {
                List<Account> batchAccounts = createAccountsBatch(roles, approvalStatuses, batch, 100);
                allAccounts.addAll(batchAccounts);
                System.out.println("✓ Batch " + (batch + 1) + ": Đã tạo " + batchAccounts.size() + " accounts");
            }

            // Tạo membership cho customers
            List<Membership> memberships = createMemberships(allAccounts);
            System.out.println("✓ Đã tạo " + memberships.size() + " memberships");

            // Tạo agency info cho agencies
            List<AgencyInfo> agencyInfos = createAgencyInfos(allAccounts, approvalStatuses);
            System.out.println("✓ Đã tạo " + agencyInfos.size() + " agency infos");

            // Tạo addresses
            List<Address> addresses = createAddressesBig(allAccounts);
            System.out.println("✓ Đã tạo " + addresses.size() + " addresses");

            // Tạo products theo batch
            List<Product> allProducts = new ArrayList<>();
            List<Account> agencies = allAccounts.stream()
                    .filter(account -> "agency".equals(account.getRole().getRoleCode()))
                    .toList();

            for (int batch = 0; batch < 20; batch++) {
                List<Product> batchProducts = createProductsBatch(categories, agencies, approvalStatuses, batch, 100);
                allProducts.addAll(batchProducts);
                System.out.println("✓ Batch " + (batch + 1) + ": Đã tạo " + batchProducts.size() + " products");
            }

            // Tạo product variants
            List<ProductVariant> productVariants = createProductVariantsBig(allProducts);
            System.out.println("✓ Đã tạo " + productVariants.size() + " product variants");

            // Tạo multimedia
            List<Multimedia> multimedias = createMultimediaBig(allAccounts, allProducts, productVariants, categories);
            System.out.println("✓ Đã tạo " + multimedias.size() + " multimedia");

            // Tạo carts
            List<Cart> carts = createCarts(allAccounts);
            System.out.println("✓ Đã tạo " + carts.size() + " carts");

            // Tạo cart items
            List<CartItem> cartItems = createCartItemsBig(carts, allProducts, productVariants);
            System.out.println("✓ Đã tạo " + cartItems.size() + " cart items");

            // Tạo coupons
            List<Coupon> coupons = createCouponsBig(categories);
            System.out.println("✓ Đã tạo " + coupons.size() + " coupons");

            // Tạo account coupons
            List<AccountCoupon> accountCoupons = createAccountCouponsBig(allAccounts, coupons);
            System.out.println("✓ Đã tạo " + accountCoupons.size() + " account coupons");

            // Tạo orders
            List<OrderList> orders = createOrdersBig(allAccounts);
            System.out.println("✓ Đã tạo " + orders.size() + " orders");

            // Tạo order details
            List<OrderDetail> orderDetails = createOrderDetailsBig(orders, allProducts, productVariants);
            System.out.println("✓ Đã tạo " + orderDetails.size() + " order details");

            // Tạo coupon order lists
            List<CouponOrderList> couponOrderLists = createCouponOrderListsBig(coupons, orders);
            System.out.println("✓ Đã tạo " + couponOrderLists.size() + " coupon order lists");

            // Tạo feedbacks
            List<Feedback> feedbacks = createFeedbacksBig(allAccounts, allProducts);
            System.out.println("✓ Đã tạo " + feedbacks.size() + " feedbacks");

            // Tính tổng cuối cùng
            long total = roleRepository.count() + approvalStatusRepository.count() +
                    parentCategoryRepository.count() + categoryRepository.count() +
                    accountRepository.count() + membershipRepository.count() +
                    agencyInfoRepository.count() + addressRepository.count() +
                    productRepository.count() + productVariantRepository.count() +
                    multimediaRepository.count() + cartRepository.count() +
                    cartItemRepository.count() + couponRepository.count() +
                    accountCouponRepository.count() + orderRepository.count() +
                    orderDetailRepository.count() + couponOrderListRepository.count() +
                    feedbackRepository.count();

            System.out.println("=== HOÀN THÀNH TẠO DỮ LIỆU MẪU LỚN ===");
            System.out.println("TỔNG SỐ RECORDS: " + total);

        } catch (Exception e) {
            System.err.println("❌ LỖI KHI TẠO DỮ LIỆU LỚN: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo dữ liệu mẫu lớn", e);
        }
    }

    // Helper methods cho việc tạo dữ liệu lớn
    private List<Category> createCategoriesBig(List<ParentCategory> parentCategories, int count) {
        List<Category> categories = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ParentCategory parentCategory = parentCategories.get(random.nextInt(parentCategories.size()));
            categories.add(Category.builder()
                    .parentCategory(parentCategory)
                    .categoryName(faker.commerce().department() + " " + (i + 1))
                    .categoryDescription(faker.lorem().sentence(8))
                    .build());
        }
        return categoryRepository.saveAll(categories);
    }

    private List<Account> createAccountsBatch(List<Role> roles, List<ApprovalStatus> approvalStatuses, int batchIndex, int batchSize) {
        List<Account> accounts = new ArrayList<>();

        Role customerRole = roles.stream().filter(r -> r.getRoleCode().equals("customer")).findFirst().orElse(roles.get(1));
        Role agencyRole = roles.stream().filter(r -> r.getRoleCode().equals("agency")).findFirst().orElse(roles.get(2));

        for (int i = 0; i < batchSize; i++) {
            int globalIndex = batchIndex * batchSize + i;
            Role role = (globalIndex % 4 == 0) ? agencyRole : customerRole; // 25% agency, 75% customer
            accounts.add(createAccount(role, approvalStatuses, globalIndex, role.getRoleCode().toLowerCase()));
        }

        return accountRepository.saveAll(accounts);
    }

    private List<Address> createAddressesBig(List<Account> accounts) {
        List<Address> addresses = new ArrayList<>();

        for (Account account : accounts) {
            int numAddresses = random.nextInt(3) + 1;
            for (int i = 0; i < numAddresses; i++) {
                addresses.add(Address.builder()
                        .account(account)
                        .fullName(account.getFullname())
                        .phone("09" + String.format("%8d", random.nextInt(100000000)))
                        .addressDetail(faker.address().fullAddress())
                        .isDefault(i == 0)
                        .build());
            }
        }

        // Save theo batch để tránh memory issue
        List<Address> savedAddresses = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i += 500) {
            int end = Math.min(i + 500, addresses.size());
            savedAddresses.addAll(addressRepository.saveAll(addresses.subList(i, end)));
        }

        return savedAddresses;
    }

    private List<Product> createProductsBatch(List<Category> categories, List<Account> agencies,
                                              List<ApprovalStatus> approvalStatuses, int batchIndex, int batchSize) {
        List<Product> products = new ArrayList<>();

        if (agencies.isEmpty()) {
            return products;
        }

        // Lấy danh sách AgencyInfo đã tạo (chỉ lấy 1 lần cho cả batch)
        List<AgencyInfo> agencyInfos = agencyInfoRepository.findAll();
        if (agencyInfos.isEmpty()) {
            System.out.println("⚠️  Không có agency info nào để tạo product batch!");
            return products;
        }

        // Tạo Map để tra cứu nhanh AgencyInfo theo Account ID
        Map<Long, AgencyInfo> agencyInfoMap = agencyInfos.stream()
                .collect(Collectors.toMap(
                        agencyInfo -> agencyInfo.getAccount().getAccountId(),
                        agencyInfo -> agencyInfo
                ));

        for (int i = 0; i < batchSize; i++) {
            int globalIndex = batchIndex * batchSize + i;
            Category category = categories.get(random.nextInt(categories.size()));
            Account selectedAgencyAccount = agencies.get(random.nextInt(agencies.size()));

            // Tìm AgencyInfo tương ứng với Account đã chọn
            AgencyInfo selectedAgencyInfo = agencyInfoMap.get(selectedAgencyAccount.getAccountId());

            if (selectedAgencyInfo == null) {
                System.out.println("⚠️  Không tìm thấy AgencyInfo cho Account ID: " + selectedAgencyAccount.getAccountId());
                continue; // Bỏ qua product này
            }

            BigDecimal listPrice = BigDecimal.valueOf(50000 + random.nextInt(9950000));
            BigDecimal salePrice = listPrice.multiply(BigDecimal.valueOf(0.7 + random.nextDouble() * 0.3));

            products.add(Product.builder()
                    .category(category)
                    .productName(faker.commerce().productName() + " " + globalIndex)
                    .productDescription(faker.lorem().paragraph(5))
                    .listPrice(listPrice)
                    .salePrice(salePrice)
                    .inventoryQuantity(random.nextInt(1000) + 10)
                    .desiredQuantity(random.nextInt(500) + 50)
                    .soldAmount(random.nextInt(200))
                    .agencyInfo(selectedAgencyInfo)  // ✅ THÊM DÒNG NÀY
                    .isSale(random.nextBoolean())
                    .approvalStatus(approvalStatuses.get(1))
                    .build());
        }

        return productRepository.saveAll(products);
    }

    private List<ProductVariant> createProductVariantsBig(List<Product> products) {
        List<ProductVariant> productVariants = new ArrayList<>();

        for (Product product : products) {
            int numVariants = random.nextInt(3) + 1;
            for (int i = 0; i < numVariants; i++) {
                BigDecimal listPrice = product.getListPrice().add(BigDecimal.valueOf(random.nextInt(500000) - 250000));
                BigDecimal salePrice = listPrice.multiply(BigDecimal.valueOf(0.7 + random.nextDouble() * 0.3));

                productVariants.add(ProductVariant.builder()
                        .product(product)
                        .productVariantName(faker.color().name() + " - " + faker.commerce().material())
                        .listPrice(listPrice)
                        .salePrice(salePrice)
                        .inventoryQuantity(random.nextInt(500) + 10)
                        .desiredQuantity(random.nextInt(200) + 20)
                        .build());
            }
        }

        // Save theo batch
        List<ProductVariant> savedVariants = new ArrayList<>();
        for (int i = 0; i < productVariants.size(); i += 1000) {
            int end = Math.min(i + 1000, productVariants.size());
            savedVariants.addAll(productVariantRepository.saveAll(productVariants.subList(i, end)));
        }

        return savedVariants;
    }

    private List<Multimedia> createMultimediaBig(List<Account> accounts, List<Product> products,
                                                 List<ProductVariant> productVariants, List<Category> categories) {
        List<Multimedia> multimedias = new ArrayList<>();

        // Account avatars
        accounts.forEach(account -> {
            multimedias.add(Multimedia.builder()
                    .account(account)
                    .multimediaUrl("https://picsum.photos/400/400?random=" + account.getAccountId())
                    .multimediaType(MultimediaType.IMAGE)
                    .build());
        });

        // Product images (2-3 per product)
        products.forEach(product -> {
            int numImages = random.nextInt(2) + 2;
            for (int i = 0; i < numImages; i++) {
                multimedias.add(Multimedia.builder()
                        .product(product)
                        .multimediaUrl("https://picsum.photos/500/500?random=" + (product.getProductId() * 10 + i))
                        .multimediaType(MultimediaType.IMAGE)
                        .build());
            }
        });

        // Save theo batch
        List<Multimedia> savedMultimedia = new ArrayList<>();
        for (int i = 0; i < multimedias.size(); i += 1000) {
            int end = Math.min(i + 1000, multimedias.size());
            savedMultimedia.addAll(multimediaRepository.saveAll(multimedias.subList(i, end)));
        }

        return savedMultimedia;
    }

    private List<CartItem> createCartItemsBig(List<Cart> carts, List<Product> products, List<ProductVariant> productVariants) {
        List<CartItem> cartItems = new ArrayList<>();

        for (Cart cart : carts) {
            int numItems = random.nextInt(5) + 1;
            for (int i = 0; i < numItems; i++) {
                Product product = products.get(random.nextInt(products.size()));
                ProductVariant variant = productVariants.stream()
                        .filter(pv -> pv.getProduct().getProductId().equals(product.getProductId()))
                        .findFirst()
                        .orElse(productVariants.get(random.nextInt(productVariants.size())));

                cartItems.add(CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .productVariant(variant)
                        .quantity(random.nextInt(5) + 1)
                        .build());
            }

            cart.setTotalItem(numItems);
            cartRepository.save(cart);
        }

        return cartItemRepository.saveAll(cartItems);
    }

    private List<Coupon> createCouponsBig(List<Category> categories) {
        List<Coupon> coupons = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            Category category = random.nextBoolean() ? categories.get(random.nextInt(categories.size())) : null;

            coupons.add(Coupon.builder()
                    .category(category)
                    .discountValue(BigDecimal.valueOf(10000 + random.nextInt(490000)))
                    .discountType(DiscountType.values()[random.nextInt(DiscountType.values().length)])
                    .expiryDate(LocalDateTime.now().plusDays(30 + random.nextInt(335)))
                    .isActivated(random.nextBoolean())
                    .remainingQuantity(10 + random.nextInt(990))
                    .minPurchaseAmount(BigDecimal.valueOf(100000 + random.nextInt(900000)))
                    .minQuantity(1 + random.nextInt(4))
                    .maxQuantity(10 + random.nextInt(40))
                    .description(faker.lorem().sentence())
                    .couponType(CouponType.values()[random.nextInt(CouponType.values().length)])
                    .build());
        }

        return couponRepository.saveAll(coupons);
    }

    private List<AccountCoupon> createAccountCouponsBig(List<Account> accounts, List<Coupon> coupons) {
        List<AccountCoupon> accountCoupons = new ArrayList<>();

        for (int i = 0; i < 2000; i++) {
            Account account = accounts.get(random.nextInt(accounts.size()));
            Coupon coupon = coupons.get(random.nextInt(coupons.size()));

            accountCoupons.add(AccountCoupon.builder()
                    .account(account)
                    .coupon(coupon)
                    .build());
        }

        return accountCouponRepository.saveAll(accountCoupons);
    }

    private List<OrderList> createOrdersBig(List<Account> accounts) {
        List<OrderList> orders = new ArrayList<>();
        List<Account> customers = accounts.stream()
                .filter(account -> "customer".equals(account.getRole().getRoleCode()))
                .toList();
        List<Account> agencies = accounts.stream()
                .filter(account -> "agency".equals(account.getRole().getRoleCode()))
                .toList();

        if (customers.isEmpty() || agencies.isEmpty()) {
            return orders;
        }

        for (int i = 0; i < 2000; i++) {
            Account customer = customers.get(random.nextInt(customers.size()));
            Account agency = agencies.get(random.nextInt(agencies.size()));

            orders.add(OrderList.builder()
                    .account(customer)
                    .agency(agency)
                    .orderDate(LocalDateTime.now().minusDays(random.nextInt(365)))
                    .totalPrice(BigDecimal.ZERO) // Sẽ được tính lại trong createOrderDetails
                    .orderStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)])
                    .addressDetail(faker.address().fullAddress())
                    .build());
        }

        return orderRepository.saveAll(orders);
    }

    private List<OrderDetail> createOrderDetailsBig(List<OrderList> orders, List<Product> products, List<ProductVariant> productVariants) {
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderList order : orders) {
            int numItems = random.nextInt(3) + 1; // 1-3 items
            BigDecimal orderTotal = BigDecimal.ZERO;

            for (int i = 0; i < numItems; i++) {
                Product product = products.get(random.nextInt(products.size()));
                ProductVariant variant = productVariants.stream()
                        .filter(pv -> pv.getProduct().getProductId().equals(product.getProductId()))
                        .findFirst()
                        .orElse(productVariants.get(random.nextInt(productVariants.size())));

                int quantity = random.nextInt(2) + 1; // 1-2 quantity để giảm tổng giá
                BigDecimal price = variant.getSalePrice();
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
                orderTotal = orderTotal.add(subtotal);

                orderDetails.add(OrderDetail.builder()
                        .orderList(order)
                        .product(product)
                        .productVariant(variant)
                        .quantity(quantity)
                        .price(price)
                        .subtotal(subtotal)
                        .build());
            }

            // Giới hạn tổng tiền order tối đa 5 triệu
            if (orderTotal.compareTo(BigDecimal.valueOf(5000000)) > 0) {
                orderTotal = BigDecimal.valueOf(500000 + random.nextInt(1500000)); // 0.5-2 triệu
            }

            order.setTotalPrice(orderTotal);
            orderRepository.save(order);
        }

        // Save theo batch
        List<OrderDetail> savedOrderDetails = new ArrayList<>();
        for (int i = 0; i < orderDetails.size(); i += 1000) {
            int end = Math.min(i + 1000, orderDetails.size());
            savedOrderDetails.addAll(orderDetailRepository.saveAll(orderDetails.subList(i, end)));
        }

        return savedOrderDetails;
    }
    private List<CouponOrderList> createCouponOrderListsBig(List<Coupon> coupons, List<OrderList> orders) {
        List<CouponOrderList> couponOrderLists = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            Coupon coupon = coupons.get(random.nextInt(coupons.size()));
            OrderList order = orders.get(random.nextInt(orders.size()));

            couponOrderLists.add(CouponOrderList.builder()
                    .coupon(coupon)
                    .orderList(order)
                    .build());
        }

        return couponOrderListRepository.saveAll(couponOrderLists);
    }

    private List<Feedback> createFeedbacksBig(List<Account> accounts, List<Product> products) {
        List<Feedback> feedbacks = new ArrayList<>();
        List<Account> customers = accounts.stream()
                .filter(account -> "customer".equals(account.getRole().getRoleCode()))
                .toList();

        if (customers.isEmpty() || products.isEmpty()) {
            return feedbacks;
        }

        for (int i = 0; i < 3000; i++) {
            Account customer = customers.get(random.nextInt(customers.size()));
            Product product = products.get(random.nextInt(products.size()));

            feedbacks.add(Feedback.builder()
                    .account(customer)
                    .product(product)
                    .rating(Rating.values()[random.nextInt(Rating.values().length)])
                    .comment(faker.lorem().paragraph(3))
                    .build());
        }

        // Save theo batch
        List<Feedback> savedFeedbacks = new ArrayList<>();
        for (int i = 0; i < feedbacks.size(); i += 1000) {
            int end = Math.min(i + 1000, feedbacks.size());
            savedFeedbacks.addAll(feedbackRepository.saveAll(feedbacks.subList(i, end)));
        }

        return savedFeedbacks;
    }
}