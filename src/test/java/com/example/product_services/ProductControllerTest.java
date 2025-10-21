package com.example.product_services;

import com.example.product_services.Adapter.controller.ProductController;
import com.example.product_services.Domains.model.Product;
import com.example.product_services.Domains.port.input.ProductUseCase;
import com.example.product_services.Infrastructure.repository.utils.TenantUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ disables real security filters
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductUseCase productUseCase;

    @MockitoBean
    private TenantUtils tenantUtils;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setProductName("Test Product");
        product.setVendorId(1L);
        product.setPrice(100.0);
        product.setStock(10);
        product.setCategory("Electronics");
        product.setTags(List.of("tech"));
    }

    @Test
    @WithMockUser(roles = "VENDOR")
    void testCreateProduct() throws Exception {
        when(tenantUtils.extractTenantId(any())).thenReturn("vendor1");

        String json = """
            {
                "productName": "Phone",
                "price": 2000.0,
                "stock": 10,
                "category": "Electronics",
                "tags": ["smartphone", "tech"]
            }
            """;

        mockMvc.perform(post("/products/vendor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("product created successfully"));

        verify(productUseCase, times(1)).createProduct(any(Product.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllProductsForAdmin() throws Exception {
        when(productUseCase.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/products/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Test Product"));

        verify(productUseCase, times(1)).getAllProducts();
    }
    @Test
    @WithMockUser(roles = "VENDOR")
    void testGetAllProductByVendorId() throws Exception {
        when(productUseCase.getAllProductsByVendorId(1L)).thenReturn(List.of(product));

        mockMvc.perform(get("/products/vendor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Test Product"));

        verify(productUseCase, times(1)).getAllProductsByVendorId(1L);
    }

    //  4. Test: Delete Product
    @Test
    @WithMockUser(roles = "VENDOR")
    void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/products/vendor/1")
                        .param("vendorId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Products deleted successfully"));

        verify(productUseCase, times(1)).deleteProduct(1L, 1L);
    }

    // 5. Test: Update Product
    @Test
    @WithMockUser(roles = "VENDOR")
    void testUpdateProduct() throws Exception {
        String json = """
            {
                "productName": "Updated Product",
                "price": 1200.0,
                "stock": 20,
                "category": "Updated",
                "tags": ["updated"]
            }
            """;

        mockMvc.perform(put("/products/1")
                        .param("vendorId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Product updated successfully"));

        verify(productUseCase, times(1))
                .updateProduct(eq(1L), any(Product.class), eq(1L));
    }

    //  6. Test: Update Product Category
    @Test
    @WithMockUser(roles = "VENDOR")
    void testUpdateCategory() throws Exception {
        when(tenantUtils.extractTenantId(any())).thenReturn("vendor1");

        mockMvc.perform(put("/products/vendor/1/category")
                        .param("category", "Gadgets"))
                .andExpect(status().isOk())
                .andExpect(content().string("Category updated successfully"));

        verify(productUseCase, times(1))
                .updateProductCategory(1L, "Gadgets", 1L);
    }

    //  7. Test: Update Product Tags
    @Test
    @WithMockUser(roles = "VENDOR")
    void testUpdateTags() throws Exception {
        when(tenantUtils.extractTenantId(any())).thenReturn("vendor1");

        String json = """
            ["updated", "popular"]
            """;

        mockMvc.perform(put("/products/vendor/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Tags updated successfully"));

        verify(productUseCase, times(1))
                .updateProductTags(eq(1L), anyList(), eq(1L));
    }

    //  8. Test: Get Products by Category
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetProductsByCategory() throws Exception {
        when(productUseCase.getProductsByCategory("Electronics")).thenReturn(List.of(product));

        mockMvc.perform(get("/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Electronics"));

        verify(productUseCase, times(1))
                .getProductsByCategory("Electronics");
    }


    @Test
    @WithMockUser(roles = "CUSTOMERS")
    void testGetProductsByTag() throws Exception {
        when(productUseCase.getProductsByTag("tech")).thenReturn(List.of(product));

        mockMvc.perform(get("/products/tag/tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tags[0]").value("tech"));

        verify(productUseCase, times(1))
                .getProductsByTag("tech");
    }
}
