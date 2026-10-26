package com.store.cosmetic.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DomainEntityTest {

    @Test
    void testUserEntity() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .name("Jane")
                .lastname("Doe")
                .email("jane@mail.com")
                .password("hash")
                .active(true)
                .dateCreated(now)
                .build();

        assertTrue(user.getActive());
        assertEquals(now, user.getDateCreated());
        assertEquals("Jane", user.getName());
    }

    @Test
    void testRoleEntity() {
        LocalDateTime now = LocalDateTime.now();
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("Admin role")
                .userCreated(1L)
                .active(true)
                .dateCreated(now)
                .build();

        assertTrue(role.getActive());
        assertEquals(now, role.getDateCreated());
        assertEquals("ADMIN", role.getName());
    }

    @Test
    void testProductEntity() {
        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder()
                .id(1L)
                .name("Lipstick")
                .description("Matte lipstick")
                .price(new BigDecimal("12.50"))
                .userCreated(1L)
                .active(true)
                .dateCreated(now)
                .build();

        assertTrue(product.getActive());
        assertEquals(now, product.getDateCreated());
        assertEquals(new BigDecimal("12.50"), product.getPrice());
    }

    @Test
    void testSaleAndDetailCompositeKey() {
        SaleStatus status = SaleStatus.builder().id(1L).status("DONE").userCreated(1L).build();

        Sale sale = Sale.builder()
                .id(1L)
                .status(status)
                .total(new BigDecimal("25.00"))
                .userCreated(1L)
                .build();

        Product product = Product.builder().id(2L).name("Serum").price(new BigDecimal("25.00")).userCreated(1L).build();

        SaleDetailId detailId = SaleDetailId.builder().id(1L).saleId(1L).productId(2L).build();
        SaleDetail detail = SaleDetail.builder()
                .id(detailId)
                .sale(sale)
                .product(product)
                .priceSale(new BigDecimal("25.00"))
                .quantity(1)
                .subtotal(new BigDecimal("25.00"))
                .userCreated(1L)
                .build();

        assertEquals(detailId, detail.getId());
        assertEquals(new BigDecimal("25.00"), detail.getSubtotal());
    }

    @Test
    void testEntryAndDetailCompositeKey() {
        EntryStatus status = EntryStatus.builder().id(1L).status("DONE").userCreated(1L).build();

        Entry entry = Entry.builder()
                .id(1L)
                .status(status)
                .isInitial(true)
                .total(new BigDecimal("100.00"))
                .userCreated(1L)
                .build();

        Product product = Product.builder().id(2L).name("Cream").price(new BigDecimal("10.00")).userCreated(1L).build();

        EntryDetailId detailId = EntryDetailId.builder().id(1L).entryId(1L).productId(2L).build();
        EntryDetail detail = EntryDetail.builder()
                .id(detailId)
                .entry(entry)
                .product(product)
                .priceEntry(new BigDecimal("10.00"))
                .quantity(10)
                .subtotal(new BigDecimal("100.00"))
                .userCreated(1L)
                .build();

        assertEquals(detailId, detail.getId());
        assertTrue(entry.getIsInitial());
        assertEquals(10, detail.getQuantity());
    }
}
