package com.example.product_services.Domains.services;

import com.example.product_services.Domains.model.Product;
import com.example.product_services.Domains.port.input.ProductUseCase;
import com.example.product_services.Domains.port.output.SavedProductPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServices implements ProductUseCase {
    private final SavedProductPort savedProductPort;

    public ProductServices(SavedProductPort savedProductPort) {
        this.savedProductPort = savedProductPort;
    }

    @Override
    public void createProduct(Product product) {
        if(product.getProductName() == null || product.getProductName().isEmpty()){
            throw  new IllegalArgumentException("product name cannot be empty");
        }
        savedProductPort.save(product);

    }



    @Override
    public void deleteProduct(Long productId, long vendorId) {
        Optional<Product> product = savedProductPort.findById(productId);
        if(product.isEmpty()){
            throw new IllegalArgumentException("Product not found");
        }
        if(!product.get().getVendorId().equals(vendorId)){
            throw new IllegalArgumentException("UnAuthorized Vendor");
        }
        savedProductPort.delete(productId);

    }

    @Override
    public List<Product> getAllProductsByVendorId(Long vendorId) {
        return savedProductPort.getAllProductsByVendorId(vendorId);
    }

    @Override
    public List<Product> getAllProducts() {
        return savedProductPort.findAll();
    }

    @Override
    public void updateProduct(Long productId, Product updatedProduct, long vendorId) {
        Optional<Product> existing = savedProductPort.findById(productId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }

        Product product = existing.get();


        if (!product.getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("Unauthorized Vendor");
        }


        product.setProductName(updatedProduct.getProductName());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());

        savedProductPort.save(product);

    }

    @Override
    public void updateProductCategory(Long productId, String category, long vendorId) {
        Product product = savedProductPort.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("Unauthorized Vendor");
        }
        product.setCategory(category);
        savedProductPort.save(product);
    }

    @Override
    public void updateProductTags(Long productId, List<String> tags, long vendorId) {
        Product product = savedProductPort.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("Unauthorized Vendor");
        }
        product.setTags(tags);
        savedProductPort.save(product);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return savedProductPort.findByCategory(category);
    }

    @Override
    public List<Product> getProductsByTag(String tag) {
        return savedProductPort.findByTag(tag);
    }
}
