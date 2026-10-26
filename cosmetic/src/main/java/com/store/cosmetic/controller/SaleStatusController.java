package com.store.cosmetic.controller;

import com.store.cosmetic.config.ApiConfig;
import com.store.cosmetic.domain.dto.response.SaleStatusResponse;
import com.store.cosmetic.service.contract.SaleService;
import com.store.cosmetic.util.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/sale-status")
public class SaleStatusController {

    private final SaleService saleService;

    public SaleStatusController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<SaleStatusResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(saleService.getAllStatuses()));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SaleStatusResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(saleService.getStatusById(id)));
    }
}
