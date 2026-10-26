package com.store.cosmetic.domain.mapper;

import com.store.cosmetic.domain.dto.response.ProductResponse;
import com.store.cosmetic.domain.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .active(product.getActive())
                .dateCreated(product.getDateCreated())
                .dateUpdated(product.getDateUpdated())
                .userCreated(product.getUserCreated())
                .userUpdated(product.getUserUpdated())
                .build();
    }
}
