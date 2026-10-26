package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.response.StockResponse;
import com.store.cosmetic.domain.model.Entry;
import com.store.cosmetic.domain.model.Product;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.EntryDetailRepository;
import com.store.cosmetic.repository.EntryRepository;
import com.store.cosmetic.repository.ProductRepository;
import com.store.cosmetic.repository.SaleDetailRepository;
import com.store.cosmetic.service.implementation.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private EntryDetailRepository entryDetailRepository;

    @Mock
    private SaleDetailRepository saleDetailRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Shampoo")
                .active(true)
                .build();
    }

    @Test
    void testCalculateStock_WithInitialCut() {
        LocalDateTime cutDate = LocalDateTime.now().minusDays(5);
        Entry initialCutEntry = Entry.builder()
                .id(10L)
                .date(cutDate)
                .isInitial(true)
                .active(true)
                .build();

        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));
        when(entryRepository.findLatestInitialEntryForProduct(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(initialCutEntry));

        // Initial cut quantity = 50
        when(entryDetailRepository.sumQuantityByEntryAndProduct(1L, 10L)).thenReturn(50);
        // Entries after cutoff = 20
        when(entryDetailRepository.sumQuantityAfterCutoff(1L, cutDate)).thenReturn(20);
        // Sales after cutoff = 15
        when(saleDetailRepository.sumQuantityAfterCutoff(1L, cutDate)).thenReturn(15);

        // Expected stock = 50 + 20 - 15 = 55
        StockResponse response = stockService.calculateStock(1L);

        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals("Shampoo", response.getProductName());
        assertEquals(55, response.getStock());
    }

    @Test
    void testCalculateStock_WithoutInitialCut() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));
        when(entryRepository.findLatestInitialEntryForProduct(eq(1L), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        when(entryDetailRepository.sumAllActiveQuantity(1L)).thenReturn(100);
        when(saleDetailRepository.sumAllActiveQuantity(1L)).thenReturn(40);

        // Expected stock = 100 - 40 = 60
        StockResponse response = stockService.calculateStock(1L);

        assertNotNull(response);
        assertEquals(60, response.getStock());
    }

    @Test
    void testCalculateStock_ProductNotFound() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockService.calculateStock(99L));
    }

    @Test
    void testCalculateStock_NeverNegative() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));
        when(entryRepository.findLatestInitialEntryForProduct(eq(1L), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        when(entryDetailRepository.sumAllActiveQuantity(1L)).thenReturn(10);
        when(saleDetailRepository.sumAllActiveQuantity(1L)).thenReturn(20);

        // Expected stock = max(10 - 20, 0) = 0
        StockResponse response = stockService.calculateStock(1L);

        assertNotNull(response);
        assertEquals(0, response.getStock());
    }
}
