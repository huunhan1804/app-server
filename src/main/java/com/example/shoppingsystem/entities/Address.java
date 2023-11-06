package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "address")
public class Address extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADDRESS_ID")
    private Long addressId;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ACCOUNT_ID", nullable = false)
    private Account account;

    @Column(name = "FULL_NAME", nullable = false)
    private String fullName;

    @Column(name = "PHONE", nullable = false)
    private String phone;

    @Column(name = "ADDRESS_DETAIL", columnDefinition = "TEXT", nullable = false)
    private String addressDetail;

    @Column(name = "IS_DEFAULT", nullable = false)
    private Boolean isDefault;
}
