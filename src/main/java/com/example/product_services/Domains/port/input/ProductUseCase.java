package com.example.product_services.Domains.port.input;

import com.example.product_services.Domains.model.Product;

import java.util.List;

public interface ProductUseCase {
    void createProduct(Product product);
    void deleteProduct(Long productId, long vendorId);
    List<Product> getAllProductsByVendorId(Long vendorId);
    List<Product> getAllProducts();  // for Admin
    void updateProduct(Long productId, Product updatedProduct, long vendorId); // for Vendor
    void updateProductCategory(Long productId, String category, long vendorId);
    void updateProductTags(Long productId, List<String> tags, long vendorId);
    List<Product> getProductsByCategory(String category);
    List<Product> getProductsByTag(String tag);
}
