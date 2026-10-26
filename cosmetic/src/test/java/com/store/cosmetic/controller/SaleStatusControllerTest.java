package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.response.SaleStatusResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.service.contract.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SaleStatusControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SaleService saleService;

    @BeforeEach
    void setUp() {
        SaleStatusController controller = new SaleStatusController(saleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllStatuses() throws Exception {
        SaleStatusResponse res = SaleStatusResponse.builder().id(1L).status("DONE").build();
        when(saleService.getAllStatuses()).thenReturn(List.of(res));

        mockMvc.perform(get("/cosmetic/api/sale-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("DONE"));
    }

    @Test
    void testGetStatusById() throws Exception {
        SaleStatusResponse res = SaleStatusResponse.builder().id(1L).status("DONE").build();
        when(saleService.getStatusById(1L)).thenReturn(res);

        mockMvc.perform(get("/cosmetic/api/sale-statuses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
