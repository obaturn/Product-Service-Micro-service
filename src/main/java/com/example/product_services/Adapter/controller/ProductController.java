package com.example.product_services.Adapter.controller;

import com.example.product_services.Domains.model.Product;
import com.example.product_services.Domains.port.input.ProductUseCase;
import com.example.product_services.Infrastructure.repository.utils.TenantUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductUseCase productPrototype;
    private final TenantUtils tenantUtils;

    public ProductController(ProductUseCase productPrototype , TenantUtils tenantUtils) {
      this.tenantUtils = tenantUtils;
      this.productPrototype=productPrototype;
    }


    @PostMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?>CreateProducts(@RequestBody Product product){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
           System.out.println("User Authorities "+auth.getAuthorities());
        System.out.println("Authenticated User: " + auth.getName());

        String tenantId = tenantUtils.extractTenantId(auth);
        System.out.println("Tenant ID from token: " + tenantId);
        Long vendorId = parseVendorId(tenantId);
        product.setVendorId(vendorId);
        productPrototype.createProduct(product);
        return ResponseEntity.ok("product created successfully");
    }
    @DeleteMapping("/vendor/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?>deleteProduct(@PathVariable Long id , @RequestParam Long vendorId){

        productPrototype.deleteProduct(id,vendorId);
        return ResponseEntity.ok("Products deleted successfully");
    }
    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasRole('VENDOR')")
    public  ResponseEntity<?>getAllProductByVendorId(@PathVariable Long vendorId){
        List<Product> products = productPrototype.getAllProductsByVendorId(vendorId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProductsForAdmin() {
        List<Product> products = productPrototype.getAllProducts();
        return ResponseEntity.ok(products);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct,
            @RequestParam Long vendorId) {

        productPrototype.updateProduct(id, updatedProduct, vendorId);
        return ResponseEntity.ok("Product updated successfully");
    }
    @PutMapping("/vendor/{id}/category")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestParam String category) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String tenantId = tenantUtils.extractTenantId(auth);
        Long vendorId = parseVendorId(tenantId);

        productPrototype.updateProductCategory(id, category, vendorId);
        return ResponseEntity.ok("Category updated successfully");
    }

    @PutMapping("/vendor/{id}/tags")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> updateTags(@PathVariable Long id, @RequestBody List<String> tags) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String tenantId = tenantUtils.extractTenantId(auth);
        Long vendorId = parseVendorId(tenantId);

        productPrototype.updateProductTags(id, tags, vendorId);
        return ResponseEntity.ok("Tags updated successfully");
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMERS')")
    public ResponseEntity<?> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productPrototype.getProductsByCategory(category));
    }

    @GetMapping("/tag/{tag}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMERS')")
    public ResponseEntity<?> getByTag(@PathVariable String tag) {
        return ResponseEntity.ok(productPrototype.getProductsByTag(tag));
    }

    private Long parseVendorId(String tenantId) {
        if (tenantId == null) throw new IllegalStateException("tenant_id missing in token");
        // tenantId expected like "vendor1" or "1" — handle both
        try {
            if (tenantId.startsWith("vendor")) {
                return Long.valueOf(tenantId.replace("vendor", ""));
            } else {
                return Long.valueOf(tenantId);
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid tenant_id format: " + tenantId);
        }
    }

}
