package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.SupportArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportArticleRepository extends JpaRepository<SupportArticle, Long> {
    List<SupportArticle> findBySupportCategory_SupportCategoryIdAndIsVisibleIsTrue(Long supportCategoryId);
    List<SupportArticle> findByIsVisibleTrue();
    List<SupportArticle> findByIsVisibleFalse();
    List<SupportArticle> findBySupportCategory_SupportCategoryId(Long supportCategoryId);
}
