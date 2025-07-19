// MembershipRepository.java
package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByAccount_AccountId(Long accountId);
}