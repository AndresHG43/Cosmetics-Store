package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.response.StockResponse;

public interface StockService {
    StockResponse calculateStock(Long productId);
}
