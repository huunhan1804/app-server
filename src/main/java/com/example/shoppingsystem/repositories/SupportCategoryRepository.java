package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.SupportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportCategoryRepository extends JpaRepository<SupportCategory, Long> {
}
