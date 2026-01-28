package com.alten.producttrial.repository;

import com.alten.producttrial.model.WishlistItem;
import com.alten.producttrial.model.User;
import com.alten.producttrial.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUser(User user);
    Optional<WishlistItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
}
