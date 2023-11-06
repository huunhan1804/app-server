package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.OrderList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderList, Long> {
    List<OrderList> findAllByAccount_AccountId(Long accountId);
}
