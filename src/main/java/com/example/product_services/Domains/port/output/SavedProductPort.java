package com.example.product_services.Domains.port.output;

import com.example.product_services.Domains.model.Product;

import java.util.List;
import java.util.Optional;

public interface SavedProductPort {
    void save(Product product);
    Optional<Product> findById(Long id);
    void delete(Long id);
    List<Product>getAllProductsByVendorId(Long vendorId);
    List<Product> findAll();
    List<Product> findByCategory(String category);
    List<Product> findByTag(String tag);


}
