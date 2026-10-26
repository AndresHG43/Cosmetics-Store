package com.store.cosmetic.controller;

import com.store.cosmetic.config.ApiConfig;
import com.store.cosmetic.domain.dto.response.EntryStatusResponse;
import com.store.cosmetic.service.contract.EntryService;
import com.store.cosmetic.util.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConfig.API_BASE_PATH + "/entry-status")
public class EntryStatusController {

    private final EntryService entryService;

    public EntryStatusController(EntryService entryService) {
        this.entryService = entryService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<EntryStatusResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(entryService.getAllStatuses()));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EntryStatusResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(entryService.getStatusById(id)));
    }
}
