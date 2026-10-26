package com.store.cosmetic.controller;

import com.store.cosmetic.domain.dto.response.EntryStatusResponse;
import com.store.cosmetic.exception.GlobalExceptionHandler;
import com.store.cosmetic.service.contract.EntryService;
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
class EntryStatusControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EntryService entryService;

    @BeforeEach
    void setUp() {
        EntryStatusController controller = new EntryStatusController(entryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetAllStatuses() throws Exception {
        EntryStatusResponse res = EntryStatusResponse.builder().id(1L).status("IN PROGRESS").build();
        when(entryService.getAllStatuses()).thenReturn(List.of(res));

        mockMvc.perform(get("/cosmetic/api/entry-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("IN PROGRESS"));
    }

    @Test
    void testGetStatusById() throws Exception {
        EntryStatusResponse res = EntryStatusResponse.builder().id(1L).status("IN PROGRESS").build();
        when(entryService.getStatusById(1L)).thenReturn(res);

        mockMvc.perform(get("/cosmetic/api/entry-status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
