// Account.java
package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "account")
public class Account extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "FULLNAME")
    private String fullname;

    @Column(name = "BIRTHDATE")
    private Date birthdate;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "IS_BANNED")
    private boolean isBanned;

    @Column(name = "LAST_LOGIN")
    private LocalDateTime lastLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACCOUNT_STATUS")
    private AccountStatus accountStatus = AccountStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "STATUS_ID")
    private ApprovalStatus approvalStatus;

    // Account Status Enum
    public enum AccountStatus {
        ACTIVE, INACTIVE, PENDING, SUSPENDED
    }

    // Custom setter for isBanned (để tương thích với code cũ)
    public void setIsBanned(boolean banned) {
        this.isBanned = banned;
    }

    // Custom getter cho isBanned (để tương thích với code cũ)
    public boolean isIsBanned() {
        return this.isBanned;
    }

    // Lombok sẽ tự động tạo isBanned() method, nhưng chúng ta cần đảm bảo tương thích
    public boolean isBanned() {
        return this.isBanned;
    }

    public void setBanned(boolean banned) {
        this.isBanned = banned;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isBanned;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE && !isBanned;
    }

    @ManyToMany
    @JoinTable(
            name = "account_coupon",
            joinColumns = @JoinColumn(name = "ACCOUNT_ID"),
            inverseJoinColumns = @JoinColumn(name = "COUPON_ID"))
    private Set<Coupon> coupons = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses;
}