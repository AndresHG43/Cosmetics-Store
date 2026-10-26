package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.request.SaleCreateRequest;
import com.store.cosmetic.domain.dto.request.SaleDetailRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.SaleResponse;
import com.store.cosmetic.domain.dto.response.SaleStatusResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.service.contract.SaleService;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SaleService saleService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SaleResponse sampleResponse;

    @BeforeEach
    void setUp() {
        SaleController controller = new SaleController(saleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = SaleResponse.builder()
                .id(1L)
                .status(SaleStatusResponse.builder().id(1L).status("IN PROGRESS").build())
                .total(new BigDecimal("150.00"))
                .details(Collections.emptyList())
                .build();
    }

    @Test
    void testCreateSale_Success() throws Exception {
        SaleDetailRequest detail = SaleDetailRequest.builder()
                .productId(1L)
                .quantity(3)
                .priceSale(new BigDecimal("50.00"))
                .build();

        SaleCreateRequest request = SaleCreateRequest.builder()
                .statusId(1L)
                .details(List.of(detail))
                .build();

        when(saleService.create(any(SaleCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/cosmetic/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetSaleById_Success() throws Exception {
        when(saleService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/cosmetic/api/sales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetAllSales_Success() throws Exception {
        PageResponse<SaleResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1)
        );
        when(saleService.getAll(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/cosmetic/api/sales")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateStatus_Success() throws Exception {
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(2L).build();
        when(saleService.updateStatus(eq(1L), any(StatusUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/cosmetic/api/sales/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteSale_Success() throws Exception {
        mockMvc.perform(delete("/cosmetic/api/sales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
