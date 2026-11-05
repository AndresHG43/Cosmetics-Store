package com.store.cosmetic.service.implementation;

import com.store.cosmetic.config.DateTimeConfig;
import com.store.cosmetic.domain.dto.request.ProductCreateRequest;
import com.store.cosmetic.domain.dto.request.ProductUpdateRequest;
import com.store.cosmetic.domain.dto.response.ProductResponse;
import com.store.cosmetic.domain.mapper.ProductMapper;
import com.store.cosmetic.domain.model.Product;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.ProductRepository;
import com.store.cosmetic.service.contract.ProductService;
import com.store.cosmetic.specification.NormalizeSpecification;
import com.store.cosmetic.util.PageResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final jakarta.servlet.http.HttpServletRequest httpServletRequest;

    private static final String PRODUCT_NOT_FOUND = "Product not found with id: ";

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductMapper productMapper,
                              jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.httpServletRequest = httpServletRequest;
    }

    private Long getAuthenticatedUserId() {
        Object userId = httpServletRequest.getAttribute("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .active(true)
                .userCreated(getAuthenticatedUserId())
                .build();
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAll(String search, String active, Pageable pageable) {
        Specification<Product> spec = buildSpec(search, Boolean.parseBoolean(active));
        return PageResponse.from(productRepository.findAll(spec, pageable)
                .map(productMapper::toResponse)
        );
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND + id));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        product.setUserUpdated(getAuthenticatedUserId());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND + id));
        product.setActive(false);
        product.setDateDeleted(LocalDateTime.now(DateTimeConfig.APP_ZONE));
        product.setUserDeleted(getAuthenticatedUserId());
        productRepository.save(product);
    }

    private Specification<Product> buildSpec(String search, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            else {
                predicates.add(cb.equal(root.get("active"), true));
            }
            if (search != null && !search.isBlank()) {
                Predicate namePred = NormalizeSpecification.normalizedContains(root.get("name"), search, cb);
                Predicate descPred = NormalizeSpecification.normalizedContains(root.get("description"), search, cb);
                predicates.add(cb.or(namePred, descPred));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
