package com.example.product_services.Adapter.persistenceProduct;

import com.example.product_services.Domains.model.Product;
import com.example.product_services.Domains.port.output.SavedProductPort;
import com.example.product_services.Infrastructure.repository.JpaProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductAdapterPersistence implements SavedProductPort {

    private final JpaProductRepository jpaRepository;

    public ProductAdapterPersistence(JpaProductRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Product product) {
        jpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Product> getAllProductsByVendorId(Long vendorId) {
        return jpaRepository.findByVendorId(vendorId);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Product> findByCategory(String category) {
        return jpaRepository.findByCategory(category);
    }

    @Override
    public List<Product> findByTag(String tag) {
        return jpaRepository.findByTagsContaining(tag);
    }
}
