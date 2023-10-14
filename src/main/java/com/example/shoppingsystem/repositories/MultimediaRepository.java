package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Account;
import com.example.shoppingsystem.entities.Multimedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MultimediaRepository  extends JpaRepository<Multimedia, Long> {

    Optional<Multimedia> findByAccount(Account account);

    Optional<Multimedia> findByProduct_ProductId(Long productId);

    Optional<Multimedia> findAllByProductVariant_ProductVariantId(Long productVariantId);

    List<Multimedia> findAllByProduct_ProductId(Long productId);

    Optional<Multimedia> findByCategory_CategoryId(Long categoryId);
}
