package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.request.EntryCreateRequest;
import com.store.cosmetic.domain.dto.request.EntryDetailRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.EntryResponse;
import com.store.cosmetic.domain.dto.response.EntryStatusResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.service.contract.EntryService;
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
class EntryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EntryService entryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private EntryResponse sampleResponse;

    @BeforeEach
    void setUp() {
        EntryController controller = new EntryController(entryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = EntryResponse.builder()
                .id(1L)
                .status(EntryStatusResponse.builder().id(1L).status("IN PROGRESS").build())
                .isInitial(false)
                .total(new BigDecimal("100.00"))
                .details(Collections.emptyList())
                .build();
    }

    @Test
    void testCreateEntry_Success() throws Exception {
        EntryDetailRequest detail = EntryDetailRequest.builder()
                .productId(1L)
                .quantity(5)
                .priceEntry(new BigDecimal("20.00"))
                .build();

        EntryCreateRequest request = EntryCreateRequest.builder()
                .statusId(1L)
                .isInitial(false)
                .details(List.of(detail))
                .build();

        when(entryService.create(any(EntryCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/cosmetic/api/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetEntryById_Success() throws Exception {
        when(entryService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/cosmetic/api/entry/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetAllEntries_Success() throws Exception {
        PageResponse<EntryResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1)
        );
        when(entryService.getAll(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/cosmetic/api/entry")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testUpdateStatus_Success() throws Exception {
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(2L).build();
        when(entryService.updateStatus(eq(1L), any(StatusUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/cosmetic/api/entry/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDeleteEntry_Success() throws Exception {
        mockMvc.perform(delete("/cosmetic/api/entry/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
