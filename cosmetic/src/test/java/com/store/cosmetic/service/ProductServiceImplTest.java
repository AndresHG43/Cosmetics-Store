package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.request.ProductCreateRequest;
import com.store.cosmetic.domain.dto.request.ProductUpdateRequest;
import com.store.cosmetic.domain.dto.response.ProductResponse;
import com.store.cosmetic.domain.mapper.ProductMapper;
import com.store.cosmetic.domain.model.Product;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.ProductRepository;
import com.store.cosmetic.service.implementation.ProductServiceImpl;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @Spy
    private ProductMapper productMapper = new ProductMapper();

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Labial Mate")
                .description("Color rojo intenso")
                .price(new BigDecimal("12.50"))
                .active(true)
                .dateCreated(LocalDateTime.now())
                .userCreated(1L)
                .build();
    }

    @Test
    void testCreateProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Labial Mate")
                .description("Color rojo intenso")
                .price(new BigDecimal("12.50"))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals("Labial Mate", response.getName());
        assertEquals(new BigDecimal("12.50"), response.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testGetById_Success() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Labial Mate", response.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getById(99L));
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<ProductResponse> response = productService.getAll("Labial", "true", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testUpdateProduct_Success() {
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .name("Labial Carmín")
                .description("Nuevo color")
                .price(new BigDecimal("14.00"))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.update(1L, request);

        assertNotNull(response);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    void testDeleteProduct_SoftDelete() {
        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));

        productService.delete(1L);

        assertFalse(sampleProduct.getActive());
        assertNotNull(sampleProduct.getDateDeleted());
        assertEquals(1L, sampleProduct.getUserDeleted());
        verify(productRepository).save(sampleProduct);
    }
}
