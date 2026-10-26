package com.store.cosmetic.service.implementation;

import com.store.cosmetic.domain.dto.request.SaleCreateRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.SaleResponse;
import com.store.cosmetic.domain.dto.response.SaleStatusResponse;
import com.store.cosmetic.domain.mapper.SaleMapper;
import com.store.cosmetic.domain.model.*;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.*;
import com.store.cosmetic.service.contract.SaleService;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final SaleStatusRepository saleStatusRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;
    private final jakarta.servlet.http.HttpServletRequest httpServletRequest;

    public SaleServiceImpl(SaleRepository saleRepository,
                           SaleDetailRepository saleDetailRepository,
                           SaleStatusRepository saleStatusRepository,
                           ProductRepository productRepository,
                           SaleMapper saleMapper,
                           jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        this.saleRepository = saleRepository;
        this.saleDetailRepository = saleDetailRepository;
        this.saleStatusRepository = saleStatusRepository;
        this.productRepository = productRepository;
        this.saleMapper = saleMapper;
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
    public SaleResponse create(SaleCreateRequest request) {
        SaleStatus status = saleStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale status not found with id: " + request.getStatusId()));

        Long currentUserId = getAuthenticatedUserId();

        LocalDateTime saleDate = request.getDate();
        if (saleDate == null) {
            if (Long.valueOf(2L).equals(status.getId()) || Long.valueOf(3L).equals(status.getId())
                    || "PENDING DELIVERY".equalsIgnoreCase(status.getStatus())
                    || "DONE".equalsIgnoreCase(status.getStatus())) {
                saleDate = LocalDateTime.now();
            }
        }

        Sale sale = Sale.builder()
                .date(saleDate)
                .status(status)
                .active(true)
                .userCreated(currentUserId)
                .build();
        sale = saleRepository.save(sale);

        List<SaleDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndActiveTrue(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + detailReq.getProductId()));
            BigDecimal subtotal = detailReq.getPriceSale().multiply(BigDecimal.valueOf(detailReq.getQuantity()));
            total = total.add(subtotal);

            SaleDetailId detailId = SaleDetailId.builder()
                    .id(saleDetailRepository.getNextDetailSequenceId())
                    .saleId(sale.getId())
                    .productId(product.getId())
                    .build();

            SaleDetail detail = SaleDetail.builder()
                    .id(detailId)
                    .sale(sale)
                    .product(product)
                    .priceSale(detailReq.getPriceSale())
                    .quantity(detailReq.getQuantity())
                    .subtotal(subtotal)
                    .active(true)
                    .userCreated(currentUserId)
                    .build();
            details.add(saleDetailRepository.save(detail));
        }

        sale.setTotal(total);
        sale = saleRepository.save(sale);

        return saleMapper.toResponse(sale, details);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        Sale sale = saleRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        List<SaleDetail> details = saleDetailRepository.findBySaleId(id);
        return saleMapper.toResponse(sale, details);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> getAll(Pageable pageable) {
        Page<SaleResponse> page = saleRepository.findByActive(true, pageable)
                .map(sale -> {
                    List<SaleDetail> details = saleDetailRepository.findBySaleId(sale.getId());
                    return saleMapper.toResponse(sale, details);
                });
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public SaleResponse updateStatus(Long id, StatusUpdateRequest request) {
        Sale sale = saleRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        SaleStatus status = saleStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale status not found with id: " + request.getStatusId()));
        Long currentUserId = getAuthenticatedUserId();
        sale.setStatus(status);
        sale.setUserUpdated(currentUserId);

        if ((Long.valueOf(2L).equals(status.getId()) || Long.valueOf(3L).equals(status.getId())
                || "PENDING DELIVERY".equalsIgnoreCase(status.getStatus())
                || "DONE".equalsIgnoreCase(status.getStatus()))
                && sale.getDate() == null) {
            sale.setDate(LocalDateTime.now());
        } else if (Long.valueOf(4L).equals(status.getId()) || "DELETED".equalsIgnoreCase(status.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            sale.setActive(false);
            sale.setDateDeleted(now);
            sale.setUserDeleted(currentUserId);

            List<SaleDetail> details = saleDetailRepository.findBySaleId(sale.getId());
            for (SaleDetail detail : details) {
                detail.setActive(false);
                detail.setDateDeleted(now);
                detail.setUserDeleted(currentUserId);
            }
            saleDetailRepository.saveAll(details);
        }

        sale = saleRepository.save(sale);
        List<SaleDetail> details = saleDetailRepository.findBySaleId(sale.getId());
        return saleMapper.toResponse(sale, details);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Sale sale = saleRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        SaleStatus status = saleStatusRepository.findById(4L)
                .orElseThrow(() -> new ResourceNotFoundException("Sale status not found with id: " + 4));

        Long currentUserId = getAuthenticatedUserId();
        LocalDateTime now = LocalDateTime.now();

        sale.setStatus(status);
        sale.setActive(false);
        sale.setDateDeleted(now);
        sale.setUserDeleted(currentUserId);
        saleRepository.save(sale);

        List<SaleDetail> details = saleDetailRepository.findBySaleId(sale.getId());
        for (SaleDetail detail : details) {
            detail.setActive(false);
            detail.setDateDeleted(now);
            detail.setUserDeleted(currentUserId);
        }
        saleDetailRepository.saveAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleStatusResponse> getAllStatuses() {
        return saleStatusRepository.findAll().stream()
                .map(saleMapper::toStatusResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaleStatusResponse getStatusById(Long id) {
        SaleStatus status = saleStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale status not found with id: " + id));
        return saleMapper.toStatusResponse(status);
    }
}
