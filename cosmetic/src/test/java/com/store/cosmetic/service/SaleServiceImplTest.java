package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.request.SaleCreateRequest;
import com.store.cosmetic.domain.dto.request.SaleDetailRequest;
import com.store.cosmetic.domain.dto.request.StatusUpdateRequest;
import com.store.cosmetic.domain.dto.response.SaleResponse;
import com.store.cosmetic.domain.mapper.SaleMapper;
import com.store.cosmetic.domain.model.Product;
import com.store.cosmetic.domain.model.Sale;
import com.store.cosmetic.domain.model.SaleDetail;
import com.store.cosmetic.domain.model.SaleStatus;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.ProductRepository;
import com.store.cosmetic.repository.SaleDetailRepository;
import com.store.cosmetic.repository.SaleRepository;
import com.store.cosmetic.repository.SaleStatusRepository;
import com.store.cosmetic.service.implementation.SaleServiceImpl;
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
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleDetailRepository saleDetailRepository;

    @Mock
    private SaleStatusRepository saleStatusRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @Spy
    private SaleMapper saleMapper = new SaleMapper();

    @InjectMocks
    private SaleServiceImpl saleService;

    private Sale sampleSale;
    private SaleStatus inProgressStatus;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        inProgressStatus = SaleStatus.builder().id(1L).status("IN PROGRESS").build();
        sampleProduct = Product.builder().id(10L).name("Crema").price(new BigDecimal("15.00")).active(true).build();
        sampleSale = Sale.builder()
                .id(200L)
                .status(inProgressStatus)
                .total(new BigDecimal("30.00"))
                .active(true)
                .dateCreated(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateSale_Success() {
        SaleDetailRequest detailReq = SaleDetailRequest.builder()
                .productId(10L)
                .priceSale(new BigDecimal("15.00"))
                .quantity(2)
                .build();

        SaleCreateRequest request = SaleCreateRequest.builder()
                .statusId(1L)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleStatusRepository.findById(1L)).thenReturn(Optional.of(inProgressStatus));
        when(saleRepository.save(any(Sale.class))).thenReturn(sampleSale);
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(saleDetailRepository.save(any(SaleDetail.class))).thenAnswer(i -> i.getArgument(0));

        SaleResponse response = saleService.create(request);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        verify(saleRepository, times(2)).save(any(Sale.class));
    }

    @Test
    void testGetById_Success() {
        when(saleRepository.findByIdAndActiveTrue(200L)).thenReturn(Optional.of(sampleSale));
        when(saleDetailRepository.findBySaleId(200L)).thenReturn(List.of());

        SaleResponse response = saleService.getById(200L);

        assertNotNull(response);
        assertEquals(200L, response.getId());
    }

    @Test
    void testGetById_NotFound() {
        when(saleRepository.findByIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> saleService.getById(999L));
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Sale> page = new PageImpl<>(List.of(sampleSale), pageable, 1);
        when(saleRepository.findByActive(true, pageable)).thenReturn(page);
        when(saleDetailRepository.findBySaleId(200L)).thenReturn(List.of());

        PageResponse<SaleResponse> response = saleService.getAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testCreateSale_InProgressWithoutDate_DateIsNull() {
        SaleDetailRequest detailReq = SaleDetailRequest.builder()
                .productId(10L)
                .priceSale(new BigDecimal("15.00"))
                .quantity(2)
                .build();

        SaleCreateRequest request = SaleCreateRequest.builder()
                .statusId(1L)
                .date(null)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleStatusRepository.findById(1L)).thenReturn(Optional.of(inProgressStatus));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(saleDetailRepository.save(any(SaleDetail.class))).thenAnswer(i -> i.getArgument(0));

        SaleResponse response = saleService.create(request);

        assertNotNull(response);
        assertNull(response.getDate());
    }

    @Test
    void testCreateSale_WithExplicitDate_PreservesDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(2);
        SaleDetailRequest detailReq = SaleDetailRequest.builder()
                .productId(10L)
                .priceSale(new BigDecimal("15.00"))
                .quantity(2)
                .build();

        SaleCreateRequest request = SaleCreateRequest.builder()
                .statusId(1L)
                .date(pastDate)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleStatusRepository.findById(1L)).thenReturn(Optional.of(inProgressStatus));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(saleDetailRepository.save(any(SaleDetail.class))).thenAnswer(i -> i.getArgument(0));

        SaleResponse response = saleService.create(request);

        assertNotNull(response);
        assertEquals(pastDate, response.getDate());
    }

    @Test
    void testCreateSale_PendingDeliveryWithoutDate_AssignsNow() {
        SaleStatus pendingStatus = SaleStatus.builder().id(2L).status("PENDING DELIVERY").build();
        SaleDetailRequest detailReq = SaleDetailRequest.builder()
                .productId(10L)
                .priceSale(new BigDecimal("15.00"))
                .quantity(2)
                .build();

        SaleCreateRequest request = SaleCreateRequest.builder()
                .statusId(2L)
                .date(null)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleStatusRepository.findById(2L)).thenReturn(Optional.of(pendingStatus));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(saleDetailRepository.save(any(SaleDetail.class))).thenAnswer(i -> i.getArgument(0));

        SaleResponse response = saleService.create(request);

        assertNotNull(response);
        assertNotNull(response.getDate());
    }

    @Test
    void testCreateSale_DoneWithoutDate_AssignsNow() {
        SaleStatus doneStatus = SaleStatus.builder().id(3L).status("DONE").build();
        SaleDetailRequest detailReq = SaleDetailRequest.builder()
                .productId(10L)
                .priceSale(new BigDecimal("15.00"))
                .quantity(2)
                .build();

        SaleCreateRequest request = SaleCreateRequest.builder()
                .statusId(3L)
                .date(null)
                .details(List.of(detailReq))
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleStatusRepository.findById(3L)).thenReturn(Optional.of(doneStatus));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(sampleProduct));
        when(saleDetailRepository.save(any(SaleDetail.class))).thenAnswer(i -> i.getArgument(0));

        SaleResponse response = saleService.create(request);

        assertNotNull(response);
        assertNotNull(response.getDate());
    }

    @Test
    void testUpdateStatus_ToPendingDeliveryWhenDateNull_AssignsNow() {
        SaleStatus pendingStatus = SaleStatus.builder().id(2L).status("PENDING DELIVERY").build();
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(2L).build();
        sampleSale.setDate(null);

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleRepository.findByIdAndActiveTrue(200L)).thenReturn(Optional.of(sampleSale));
        when(saleStatusRepository.findById(2L)).thenReturn(Optional.of(pendingStatus));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(saleDetailRepository.findBySaleId(200L)).thenReturn(List.of());

        SaleResponse response = saleService.updateStatus(200L, request);

        assertNotNull(response);
        assertNotNull(sampleSale.getDate());
    }

    @Test
    void testUpdateStatus_ToDonePreservesExistingDate() {
        LocalDateTime existingDate = LocalDateTime.now().minusDays(1);
        SaleStatus doneStatus = SaleStatus.builder().id(3L).status("DONE").build();
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(3L).build();
        sampleSale.setDate(existingDate);

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleRepository.findByIdAndActiveTrue(200L)).thenReturn(Optional.of(sampleSale));
        when(saleStatusRepository.findById(3L)).thenReturn(Optional.of(doneStatus));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(saleDetailRepository.findBySaleId(200L)).thenReturn(List.of());

        SaleResponse response = saleService.updateStatus(200L, request);

        assertNotNull(response);
        assertEquals(existingDate, sampleSale.getDate());
    }

    @Test
    void testUpdateStatus_ToDeleted_SoftDeletesSaleAndDetails() {
        SaleStatus deletedStatus = SaleStatus.builder().id(4L).status("DELETED").build();
        StatusUpdateRequest request = StatusUpdateRequest.builder().statusId(4L).build();

        SaleDetail detail1 = SaleDetail.builder().active(true).build();
        SaleDetail detail2 = SaleDetail.builder().active(true).build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleRepository.findByIdAndActiveTrue(200L)).thenReturn(Optional.of(sampleSale));
        when(saleStatusRepository.findById(4L)).thenReturn(Optional.of(deletedStatus));
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
        when(saleDetailRepository.findBySaleId(200L)).thenReturn(List.of(detail1, detail2));

        saleService.updateStatus(200L, request);

        assertFalse(sampleSale.getActive());
        assertNotNull(sampleSale.getDateDeleted());
        assertEquals(1L, sampleSale.getUserDeleted());

        assertFalse(detail1.getActive());
        assertNotNull(detail1.getDateDeleted());
        assertEquals(1L, detail1.getUserDeleted());
        assertFalse(detail2.getActive());
        assertNotNull(detail2.getDateDeleted());
        assertEquals(1L, detail2.getUserDeleted());

        verify(saleDetailRepository).saveAll(List.of(detail1, detail2));
    }

    @Test
    void testDeleteSale_SoftDelete() {
        SaleStatus deletedStatus = SaleStatus.builder().id(4L).status("DELETED").build();
        SaleDetail detail1 = SaleDetail.builder().active(true).build();
        SaleDetail detail2 = SaleDetail.builder().active(true).build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(saleRepository.findByIdAndActiveTrue(200L)).thenReturn(Optional.of(sampleSale));
        when(saleStatusRepository.findById(4L)).thenReturn(Optional.of(deletedStatus));
        when(saleDetailRepository.findBySaleId(200L)).thenReturn(List.of(detail1, detail2));

        saleService.delete(200L);

        assertFalse(sampleSale.getActive());
        assertEquals(deletedStatus, sampleSale.getStatus());
        assertNotNull(sampleSale.getDateDeleted());
        assertEquals(1L, sampleSale.getUserDeleted());
        verify(saleRepository).save(sampleSale);

        assertFalse(detail1.getActive());
        assertNotNull(detail1.getDateDeleted());
        assertEquals(1L, detail1.getUserDeleted());
        assertFalse(detail2.getActive());
        assertNotNull(detail2.getDateDeleted());
        assertEquals(1L, detail2.getUserDeleted());

        verify(saleDetailRepository).saveAll(List.of(detail1, detail2));
    }
}
