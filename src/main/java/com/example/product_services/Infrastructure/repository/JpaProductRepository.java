package com.example.product_services.Infrastructure.repository;

import com.example.product_services.Domains.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaProductRepository extends JpaRepository<Product , Long> {
    List<Product> findByVendorId(Long vendorId);
    List<Product> findByCategory(String category);
    List<Product> findByTagsContaining(String tag);




}
