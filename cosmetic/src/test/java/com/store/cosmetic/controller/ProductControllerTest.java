package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.request.ProductCreateRequest;
import com.store.cosmetic.domain.dto.request.ProductUpdateRequest;
import com.store.cosmetic.domain.dto.response.ProductResponse;
import com.store.cosmetic.domain.dto.response.StockResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.service.contract.ProductService;
import com.store.cosmetic.service.contract.StockService;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private StockService stockService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductResponse sampleResponse;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService, stockService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = ProductResponse.builder()
                .id(1L)
                .name("Labial Rojo")
                .description("Mate")
                .price(new BigDecimal("15.50"))
                .active(true)
                .build();
    }

    @Test
    void testCreateProduct_Success() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Labial Rojo")
                .description("Mate")
                .price(new BigDecimal("15.50"))
                .build();

        when(productService.create(any(ProductCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/cosmetic/api/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Labial Rojo"));
    }

    @Test
    void testGetProductById_Success() throws Exception {
        when(productService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/cosmetic/api/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetProductStock_Success() throws Exception {
        StockResponse stockResponse = StockResponse.builder()
                .productId(1L)
                .productName("Labial Rojo")
                .stock(42)
                .build();

        when(stockService.calculateStock(1L)).thenReturn(stockResponse);

        mockMvc.perform(get("/cosmetic/api/product/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stock").value(42));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productService.getById(99L)).thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/cosmetic/api/product/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetAllProducts_Paginated() throws Exception {
        PageResponse<ProductResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1)
        );
        when(productService.getAll(any(), any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/cosmetic/api/product")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Labial Rojo"));
    }

    @Test
    void testUpdateProduct_Success() throws Exception {
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .name("Labial Carmín")
                .description("Nuevo color mate")
                .price(new BigDecimal("18.00"))
                .build();

        when(productService.update(eq(1L), any(ProductUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/cosmetic/api/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/cosmetic/api/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
