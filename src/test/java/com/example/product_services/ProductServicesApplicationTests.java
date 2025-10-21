package com.example.product_services;

import com.example.product_services.Domains.model.Product;
import com.example.product_services.Domains.port.output.OutBoxPort;
import com.example.product_services.Domains.port.output.SavedProductPort;
import com.example.product_services.Domains.services.ProductServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServicesApplicationTests {

	private SavedProductPort saved;
	private ProductServices productServices;
    private OutBoxPort outBoxPort;
	@BeforeEach
	void setUp() {
		saved = mock(SavedProductPort.class);
        outBoxPort = mock(OutBoxPort.class);
		productServices = new ProductServices(saved,outBoxPort);
	}

    @Test
    void test_that_create_product_is_successful() {
        Product pro = new Product();
        pro.setProductName("Apple-Laptop");
        pro.setId(1L);
        pro.setStock(3);
        pro.setVendorId(2L);
        pro.setPrice(5.00);
        pro.setCategory("Phone");
        pro.setTags(List.of("Iphone","Nokia"));
        productServices.createProduct(pro);

        verify(saved, times(1)).save(pro);
        verify(outBoxPort, times(1))
                .saveEvent(pro, "product-topic", "PRODUCT_CREATED"); // ✅ also verify event
    }

	@Test
	void test_That_Create_Product_Fails_If_Name_Is_Empty(){
		Product pro = new Product();
		pro.setProductName("");
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				()->productServices.createProduct(pro));
		assertEquals("product name cannot be empty",ex.getMessage());
		verify(saved,never()).save(any());

	}
	@Test
	void test_That_Delete_Product_Is_A_Success(){
		Long productId = 1L;
		Long vendorId =12L;
		Product product = new Product();
		product.setId(productId);
		product.setVendorId(vendorId);
		when(saved.findById(productId)).thenReturn(Optional.of(product));
		productServices.deleteProduct(productId,vendorId);
		verify(saved,times(1)).delete(productId);
        verify(outBoxPort, times(1))
                .saveEvent(product, "product-topic", "PRODUCT_DELETED");

	}
	@Test
	void testDeleteProduct_ProductNotFound() {
		Long productId = 1L;
		Long vendorId = 123L;

		when(saved.findById(productId)).thenReturn(Optional.empty());

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
				productServices.deleteProduct(productId, vendorId));

		assertEquals("Product not found", ex.getMessage());
		verify(saved, never()).delete(any());
	}
    @Test
    void testDeleteProduct_UnauthorizedVendor() {
        Long productId = 1L;
        Long vendorId = 123L;
        Product product = new Product();
        product.setId(productId);
        product.setVendorId(999L); // Different vendor

        when(saved.findById(productId)).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                productServices.deleteProduct(productId, vendorId));

        assertEquals("UnAuthorized Vendor", ex.getMessage());
        verify(saved, never()).delete(any());
    }

    @Test
    void testUpdateProduct_Success() {
        Long productId = 1L;
        Long vendorId = 2L;
        Product existing = new Product();
        existing.setId(productId);
        existing.setVendorId(vendorId);
        existing.setProductName("Old Product");
        existing.setPrice(10.0);
        existing.setStock(5);

        Product updated = new Product();
        updated.setProductName("New Product");
        updated.setPrice(20.0);
        updated.setStock(10);

        when(saved.findById(productId)).thenReturn(Optional.of(existing));

        productServices.updateProduct(productId, updated, vendorId);

        verify(saved, times(1)).save(existing);
        assertEquals("New Product", existing.getProductName());
        assertEquals(20.0, existing.getPrice());
        assertEquals(10, existing.getStock());
    }

    @Test
    void testUpdateProductCategory_Success() {
        Long productId = 1L;
        Long vendorId = 2L;
        Product product = new Product();
        product.setId(productId);
        product.setVendorId(vendorId);
        product.setCategory("Old Category");

        when(saved.findById(productId)).thenReturn(Optional.of(product));

        productServices.updateProductCategory(productId, "Electronics", vendorId);

        verify(saved, times(1)).save(product);
        assertEquals("Electronics", product.getCategory());
    }

    @Test
    void testUpdateProductTags_Success() {
        Long productId = 1L;
        Long vendorId = 2L;
        Product product = new Product();
        product.setId(productId);
        product.setVendorId(vendorId);
        product.setTags(List.of("old"));

        when(saved.findById(productId)).thenReturn(Optional.of(product));

        productServices.updateProductTags(productId, List.of("new", "popular"), vendorId);

        verify(saved, times(1)).save(product);
        assertEquals(List.of("new", "popular"), product.getTags());
    }

    @Test
    void testGetProductsByCategory() {
        when(saved.findByCategory("Electronics")).thenReturn(List.of(new Product()));

        var result = productServices.getProductsByCategory("Electronics");

        verify(saved, times(1)).findByCategory("Electronics");
        assertEquals(1, result.size());
    }

    @Test
    void testGetProductsByTag() {
        when(saved.findByTag("popular")).thenReturn(List.of(new Product()));

        var result = productServices.getProductsByTag("popular");

        verify(saved, times(1)).findByTag("popular");
        assertEquals(1, result.size());
    }

}
