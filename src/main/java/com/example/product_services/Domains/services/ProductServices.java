package com.example.product_services.Domains.services;

import com.example.product_services.Domains.model.Product;
import com.example.product_services.Domains.port.input.ProductUseCase;
import com.example.product_services.Domains.port.output.OutBoxPort;
import com.example.product_services.Domains.port.output.SavedProductPort;
import com.example.product_services.Infrastructure.repository.Dto.ProductEvent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServices implements ProductUseCase {
    private final SavedProductPort savedProductPort;
    private final OutBoxPort outBoxPort;

    public ProductServices(SavedProductPort savedProductPort , OutBoxPort outBoxPort) {
        this.savedProductPort = savedProductPort;
        this.outBoxPort=outBoxPort;
    }

    @Override
    @Transactional
    public void createProduct(Product product) {
        if(product.getProductName() == null || product.getProductName().isEmpty()){
            throw  new IllegalArgumentException("product name cannot be empty");
        }
        savedProductPort.save(product);
        ProductEvent event = buildEvent(product, "PRODUCT_CREATED");
        outBoxPort.saveEvent(event, "product-topic", event.getEventType());

    }



    @Override
    @Transactional
    public void deleteProduct(Long productId, long vendorId) {
        Optional<Product> product = savedProductPort.findById(productId);
        if(product.isEmpty()){
            throw new IllegalArgumentException("Product not found");
        }
        if(!product.get().getVendorId().equals(vendorId)){
            throw new IllegalArgumentException("UnAuthorized Vendor");
        }
        savedProductPort.delete(productId);
        ProductEvent event = buildEvent(product.get(), "PRODUCT_DELETED");
        outBoxPort.saveEvent(event, "product-topic", event.getEventType());

    }
    private ProductEvent buildEvent(Product product, String eventType) {
        ProductEvent event = new ProductEvent();
        event.setEventType(eventType);
        event.setProductId(product.getId());
        event.setProductName(product.getProductName());
        event.setPrice(product.getPrice());
        event.setVendorId(product.getVendorId());
        event.setCategory(product.getCategory());
        event.setTags(product.getTags());
        return event;
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
    @Transactional
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
        ProductEvent event = buildEvent(product, "PRODUCT_UPDATED");
        outBoxPort.saveEvent(event, "product-topic", event.getEventType());


    }

    @Override
    @Transactional
    public void updateProductCategory(Long productId, String category, long vendorId) {
        Product product = savedProductPort.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("Unauthorized Vendor");
        }
        product.setCategory(category);
        savedProductPort.save(product);
        ProductEvent event = buildEvent(product, "CATEGORY_UPDATED");
        outBoxPort.saveEvent(event, "product-topic", event.getEventType());
    }

    @Override
    @Transactional
    public void updateProductTags(Long productId, List<String> tags, long vendorId) {
        Product product = savedProductPort.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (!product.getVendorId().equals(vendorId)) {
            throw new IllegalArgumentException("Unauthorized Vendor");
        }
        product.setTags(tags);
        savedProductPort.save(product);
        ProductEvent event = buildEvent(product, "TAGS_UPDATED");
        outBoxPort.saveEvent(event, "product-topic", event.getEventType());
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
