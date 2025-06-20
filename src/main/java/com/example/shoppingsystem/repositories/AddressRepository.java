package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByAccount_AccountId(Long accountId);
    Optional<Address> findByAccount_AccountIdAndAddressId(Long accountId, Long addressId);
}
