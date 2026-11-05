package com.store.cosmetic.service.implementation;

import com.store.cosmetic.config.DateTimeConfig;
import com.store.cosmetic.domain.dto.request.EntryCreateRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.EntryResponse;
import com.store.cosmetic.domain.dto.response.EntryStatusResponse;
import com.store.cosmetic.domain.mapper.EntryMapper;
import com.store.cosmetic.domain.model.*;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.*;
import com.store.cosmetic.service.contract.EntryService;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EntryServiceImpl implements EntryService {

    private final EntryRepository entryRepository;
    private final EntryDetailRepository entryDetailRepository;
    private final EntryStatusRepository entryStatusRepository;
    private final ProductRepository productRepository;
    private final EntryMapper entryMapper;
    private final jakarta.servlet.http.HttpServletRequest httpServletRequest;

    private static final String ENTRY_STATUS_NOT_FOUND = "Entry status not found with id: ";
    private static final String ENTRY_NOT_FOUND = "Entry not found with id: ";

    public EntryServiceImpl(EntryRepository entryRepository,
                            EntryDetailRepository entryDetailRepository,
                            EntryStatusRepository entryStatusRepository,
                            ProductRepository productRepository,
                            EntryMapper entryMapper,
                            jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        this.entryRepository = entryRepository;
        this.entryDetailRepository = entryDetailRepository;
        this.entryStatusRepository = entryStatusRepository;
        this.productRepository = productRepository;
        this.entryMapper = entryMapper;
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
    public EntryResponse create(EntryCreateRequest request) {
        EntryStatus status = entryStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(ENTRY_STATUS_NOT_FOUND + request.getStatusId()));

        Long currentUserId = getAuthenticatedUserId();
        LocalDateTime entryDate = request.getDate();
        if (entryDate == null && Long.valueOf(2L).equals(status.getId())) {
            entryDate = LocalDateTime.now(DateTimeConfig.APP_ZONE);
        }

        Entry entry = Entry.builder()
                .isInitial(Boolean.TRUE.equals(request.getIsInitial()))
                .status(status)
                .active(true)
                .userCreated(currentUserId)
                .date(entryDate)
                .build();
        entry = entryRepository.save(entry);

        List<EntryDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var detailReq : request.getDetails()) {
            Product product = productRepository.findByIdAndActiveTrue(detailReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + detailReq.getProductId()));
            BigDecimal subtotal = detailReq.getPriceEntry().multiply(BigDecimal.valueOf(detailReq.getQuantity()));
            total = total.add(subtotal);

            EntryDetailId detailId = EntryDetailId.builder()
                    .id(entryDetailRepository.getNextDetailSequenceId())
                    .entryId(entry.getId())
                    .productId(product.getId())
                    .build();

            EntryDetail detail = EntryDetail.builder()
                    .id(detailId)
                    .entry(entry)
                    .product(product)
                    .priceEntry(detailReq.getPriceEntry())
                    .quantity(detailReq.getQuantity())
                    .subtotal(subtotal)
                    .active(true)
                    .userCreated(currentUserId)
                    .build();
            details.add(entryDetailRepository.save(detail));
        }

        entry.setTotal(total);
        entry = entryRepository.save(entry);

        return entryMapper.toResponse(entry, details);
    }

    @Override
    @Transactional(readOnly = true)
    public EntryResponse getById(Long id) {
        Entry entry = entryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTRY_NOT_FOUND + id));
        List<EntryDetail> details = entryDetailRepository.findByEntryId(id);
        return entryMapper.toResponse(entry, details);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EntryResponse> getAll(Pageable pageable) {
        Page<EntryResponse> page = entryRepository.findByActive(true, pageable)
                .map(entry -> {
                    List<EntryDetail> details = entryDetailRepository.findByEntryId(entry.getId());
                    return entryMapper.toResponse(entry, details);
                });
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public EntryResponse updateStatus(Long id, StatusUpdateRequest request) {
        Entry entry = entryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTRY_NOT_FOUND + id));
        EntryStatus status = entryStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException(ENTRY_STATUS_NOT_FOUND + request.getStatusId()));
        Long currentUserId = getAuthenticatedUserId();
        entry.setStatus(status);
        entry.setUserUpdated(currentUserId);

        if (Long.valueOf(2L).equals(status.getId()) && entry.getDate() == null) {
            entry.setDate(LocalDateTime.now(DateTimeConfig.APP_ZONE));
        } else if (Long.valueOf(3L).equals(status.getId()) || "DELETED".equalsIgnoreCase(status.getStatus())) {
            entry.setActive(false);
            entry.setDateDeleted(LocalDateTime.now(DateTimeConfig.APP_ZONE));
            entry.setUserDeleted(currentUserId);

            List<EntryDetail> details = entryDetailRepository.findByEntryId(entry.getId());
            for (EntryDetail detail : details) {
                detail.setActive(false);
                detail.setDateDeleted(LocalDateTime.now(DateTimeConfig.APP_ZONE));
                detail.setUserDeleted(currentUserId);
            }
            entryDetailRepository.saveAll(details);
        }

        entry = entryRepository.save(entry);
        List<EntryDetail> details = entryDetailRepository.findByEntryId(entry.getId());
        return entryMapper.toResponse(entry, details);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Entry entry = entryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTRY_NOT_FOUND + id));

        EntryStatus status = entryStatusRepository.findById(3L)
                .orElseThrow(() -> new ResourceNotFoundException(ENTRY_STATUS_NOT_FOUND + 3));

        Long currentUserId = getAuthenticatedUserId();
        LocalDateTime now = LocalDateTime.now(DateTimeConfig.APP_ZONE);

        entry.setActive(false);
        entry.setStatus(status);
        entry.setDateDeleted(now);
        entry.setUserDeleted(currentUserId);
        entryRepository.save(entry);

        List<EntryDetail> details = entryDetailRepository.findByEntryId(entry.getId());
        for (EntryDetail detail : details) {
            detail.setActive(false);
            detail.setDateDeleted(now);
            detail.setUserDeleted(currentUserId);
        }
        entryDetailRepository.saveAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntryStatusResponse> getAllStatuses() {
        return entryStatusRepository.findAll().stream()
                .map(entryMapper::toStatusResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EntryStatusResponse getStatusById(Long id) {
        EntryStatus status = entryStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTRY_STATUS_NOT_FOUND + id));
        return entryMapper.toStatusResponse(status);
    }
}
