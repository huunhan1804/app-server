package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Multimedia;
import com.example.shoppingsystem.enums.MultimediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MultimediaRepository extends JpaRepository<Multimedia, Long> {

    @Query("SELECT m.multimediaUrl FROM Multimedia m WHERE m.product.productId = :productId AND m.isPrimary = true")
    String findPrimaryImageByProductId(@Param("productId") Long productId);

    @Query("SELECT NEW map(m.multimediaId as multimediaId, m.multimediaUrl as multimediaUrl, " +
            "m.isPrimary as isPrimary, m.displayOrder as displayOrder) " +
            "FROM Multimedia m WHERE m.product.productId = :productId AND m.multimediaType = :type " +
            "ORDER BY m.isPrimary DESC, m.displayOrder ASC")
    List<Map<String, Object>> findImagesByProductId(@Param("productId") Long productId,
                                                    @Param("type") MultimediaType type);

    List<Multimedia> findByProduct_ProductIdAndMultimediaType(Long productId, MultimediaType type);

    List<Multimedia> findByAccount_AccountId(Long accountId);
}
