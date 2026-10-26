package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.request.EntryCreateRequest;
import com.store.cosmetic.domain.dto.request.EntryDetailRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.EntryResponse;
import com.store.cosmetic.domain.mapper.EntryMapper;
import com.store.cosmetic.domain.model.Entry;
import com.store.cosmetic.domain.model.EntryDetail;
import com.store.cosmetic.domain.model.EntryStatus;
import com.store.cosmetic.domain.model.Product;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.EntryDetailRepository;
import com.store.cosmetic.repository.EntryRepository;
import com.store.cosmetic.repository.EntryStatusRepository;
import com.store.cosmetic.repository.ProductRepository;
import com.store.cosmetic.service.implementation.EntryServiceImpl;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntryServiceImplTest {

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private EntryDetailRepository entryDetailRepository;

    @Mock
    private EntryStatusRepository entryStatusRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @Spy
    private EntryMapper entryMapper = new EntryMapper();

    @InjectMocks
    private EntryServiceImpl entryService;

    private Entry sampleEntry;
    private EntryStatus inProgressStatus;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        inProgressStatus = EntryStatus.builder().id(1L).status("IN PROGRESS").build();
        sampleProduct = Product.builder().id(10L).name("Perfume").price(new BigDecimal("25.00")).active(true).build();
        sampleEntry = Entry.builder()
                .id(100L)
                .status(inProgressStatus)
                .isInitial(false)
                .total(new BigDecimal("50.00"))
                .active(true)
                .dateCreated(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateEntry_Success() {
        EntryDetailRequest detailReq = EntryDetailRequest.builder()
                .productId(10L)
                .priceEntry(new BigDecimal("25.00"))
                .quantity(2)
                .build();

        EntryCreateRequest request = EntryCreateRequest.builder()
                .statusId(1L)
                .isInitial(false)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryStatusRepository.findById(1L)).thenReturn(Optional.of(inProgressStatus));
        when(entryRepository.save(any(Entry.class))).thenReturn(sampleEntry);
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(entryDetailRepository.save(any(EntryDetail.class))).thenAnswer(i -> i.getArgument(0));

        EntryResponse response = entryService.create(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        verify(entryRepository, times(2)).save(any(Entry.class));
    }

    @Test
    void testGetById_Success() {
        when(entryRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleEntry));
        when(entryDetailRepository.findByEntryId(100L)).thenReturn(List.of());

        EntryResponse response = entryService.getById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void testGetById_NotFound() {
        when(entryRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> entryService.getById(999L));
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Entry> page = new PageImpl<>(List.of(sampleEntry), pageable, 1);
        when(entryRepository.findByActive(true, pageable)).thenReturn(page);
        when(entryDetailRepository.findByEntryId(100L)).thenReturn(List.of());

        PageResponse<EntryResponse> response = entryService.getAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testUpdateStatus_Success() {
        EntryStatus doneStatus = EntryStatus.builder().id(2L).status("DONE").build();
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(2L).build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleEntry));
        when(entryStatusRepository.findById(2L)).thenReturn(Optional.of(doneStatus));
        when(entryRepository.save(any(Entry.class))).thenReturn(sampleEntry);
        when(entryDetailRepository.findByEntryId(100L)).thenReturn(List.of());

        EntryResponse response = entryService.updateStatus(100L, request);

        assertNotNull(response);
        verify(entryRepository).save(sampleEntry);
    }

    @Test
    void testCreateEntry_WithExplicitDate() {
        LocalDateTime customDate = LocalDateTime.of(2026, 1, 1, 10, 0);
        EntryDetailRequest detailReq = EntryDetailRequest.builder()
                .productId(10L)
                .priceEntry(new BigDecimal("25.00"))
                .quantity(2)
                .build();

        EntryCreateRequest request = EntryCreateRequest.builder()
                .statusId(1L)
                .date(customDate)
                .isInitial(false)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryStatusRepository.findById(1L)).thenReturn(Optional.of(inProgressStatus));
        when(entryRepository.save(any(Entry.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(entryDetailRepository.save(any(EntryDetail.class))).thenAnswer(i -> i.getArgument(0));

        EntryResponse response = entryService.create(request);

        assertNotNull(response);
        assertEquals(customDate, response.getDate());
    }

    @Test
    void testCreateEntry_InProgressWithoutDate_DateIsNull() {
        EntryDetailRequest detailReq = EntryDetailRequest.builder()
                .productId(10L)
                .priceEntry(new BigDecimal("25.00"))
                .quantity(2)
                .build();

        EntryCreateRequest request = EntryCreateRequest.builder()
                .statusId(1L)
                .date(null)
                .isInitial(false)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryStatusRepository.findById(1L)).thenReturn(Optional.of(inProgressStatus));
        when(entryRepository.save(any(Entry.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(entryDetailRepository.save(any(EntryDetail.class))).thenAnswer(i -> i.getArgument(0));

        EntryResponse response = entryService.create(request);

        assertNotNull(response);
        assertNull(response.getDate());
    }

    @Test
    void testCreateEntry_DoneWithoutDate_DateIsAssignedNow() {
        EntryStatus doneStatus = EntryStatus.builder().id(2L).status("DONE").build();
        EntryDetailRequest detailReq = EntryDetailRequest.builder()
                .productId(10L)
                .priceEntry(new BigDecimal("25.00"))
                .quantity(2)
                .build();

        EntryCreateRequest request = EntryCreateRequest.builder()
                .statusId(2L)
                .date(null)
                .isInitial(false)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryStatusRepository.findById(2L)).thenReturn(Optional.of(doneStatus));
        when(entryRepository.save(any(Entry.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(entryDetailRepository.save(any(EntryDetail.class))).thenAnswer(i -> i.getArgument(0));

        EntryResponse response = entryService.create(request);

        assertNotNull(response);
        assertNotNull(response.getDate());
    }

    @Test
    void testUpdateStatus_ToDoneWhenDateIsNull_AssignsNow() {
        EntryStatus doneStatus = EntryStatus.builder().id(2L).status("DONE").build();
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(2L).build();
        sampleEntry.setDate(null);

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleEntry));
        when(entryStatusRepository.findById(2L)).thenReturn(Optional.of(doneStatus));
        when(entryRepository.save(any(Entry.class))).thenAnswer(i -> i.getArgument(0));
        when(entryDetailRepository.findByEntryId(100L)).thenReturn(List.of());

        EntryResponse response = entryService.updateStatus(100L, request);

        assertNotNull(response);
        assertNotNull(sampleEntry.getDate());
    }

    @Test
    void testUpdateStatus_ToDeleted_SoftDeletesEntry() {
        EntryStatus deletedStatus = EntryStatus.builder().id(3L).status("DELETED").build();
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(3L).build();

        EntryDetail detail1 = EntryDetail.builder().active(true).build();
        EntryDetail detail2 = EntryDetail.builder().active(true).build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleEntry));
        when(entryStatusRepository.findById(3L)).thenReturn(Optional.of(deletedStatus));
        when(entryRepository.save(any(Entry.class))).thenAnswer(i -> i.getArgument(0));
        when(entryDetailRepository.findByEntryId(100L)).thenReturn(List.of(detail1, detail2));

        entryService.updateStatus(100L, request);

        assertFalse(sampleEntry.getActive());
        assertNotNull(sampleEntry.getDateDeleted());
        assertEquals(1L, sampleEntry.getUserDeleted());

        assertFalse(detail1.getActive());
        assertNotNull(detail1.getDateDeleted());
        assertEquals(1L, detail1.getUserDeleted());
        assertFalse(detail2.getActive());
        assertNotNull(detail2.getDateDeleted());
        assertEquals(1L, detail2.getUserDeleted());

        verify(entryDetailRepository).saveAll(List.of(detail1, detail2));
    }

    @Test
    void testDeleteEntry_SoftDelete() {
        EntryStatus deletedStatus = EntryStatus.builder().id(3L).status("DELETED").build();
        EntryDetail detail1 = EntryDetail.builder().active(true).build();
        EntryDetail detail2 = EntryDetail.builder().active(true).build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(entryRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(sampleEntry));
        when(entryStatusRepository.findById(3L)).thenReturn(Optional.of(deletedStatus));
        when(entryDetailRepository.findByEntryId(100L)).thenReturn(List.of(detail1, detail2));

        entryService.delete(100L);

        assertFalse(sampleEntry.getActive());
        assertEquals(deletedStatus, sampleEntry.getStatus());
        assertNotNull(sampleEntry.getDateDeleted());
        assertEquals(1L, sampleEntry.getUserDeleted());
        verify(entryRepository).save(sampleEntry);

        assertFalse(detail1.getActive());
        assertNotNull(detail1.getDateDeleted());
        assertEquals(1L, detail1.getUserDeleted());
        assertFalse(detail2.getActive());
        assertNotNull(detail2.getDateDeleted());
        assertEquals(1L, detail2.getUserDeleted());

        verify(entryDetailRepository).saveAll(List.of(detail1, detail2));
    }
}
