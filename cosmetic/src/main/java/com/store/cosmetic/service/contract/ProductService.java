package com.store.cosmetic.service.contract;

import com.store.cosmetic.domain.dto.request.ProductCreateRequest;
import com.store.cosmetic.domain.dto.request.ProductUpdateRequest;
import com.store.cosmetic.domain.dto.response.ProductResponse;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse create(ProductCreateRequest request);
    ProductResponse getById(Long id);
    PageResponse<ProductResponse> getAll(String search, String active, Pageable pageable);
    ProductResponse update(Long id, ProductUpdateRequest request);
    void delete(Long id);
}
