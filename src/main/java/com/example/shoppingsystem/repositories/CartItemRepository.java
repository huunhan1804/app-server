package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Cart;
import com.example.shoppingsystem.entities.CartItem;
import com.example.shoppingsystem.entities.Product;
import com.example.shoppingsystem.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByCart_CartId(Long cartId);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);
}
