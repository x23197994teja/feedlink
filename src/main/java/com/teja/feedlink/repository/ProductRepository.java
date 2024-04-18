package com.teja.feedlink.repository;

import com.teja.feedlink.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByStatus(String status);

    List<Product> findByStatusAndPostedBy(String status, String postedBy);
}
