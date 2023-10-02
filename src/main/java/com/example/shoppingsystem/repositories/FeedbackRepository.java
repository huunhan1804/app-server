package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByProduct_ProductId(Long productId);
}
