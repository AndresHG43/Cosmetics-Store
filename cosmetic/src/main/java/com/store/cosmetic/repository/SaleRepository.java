package com.store.cosmetic.repository;

import com.store.cosmetic.domain.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {
    Page<Sale> findByActive(Boolean active, Pageable pageable);
    Optional<Sale> findByIdAndActiveTrue(Long id);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Sale s WHERE s.active = true AND s.status.status <> 'DELETED' " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR s.date >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR s.date <= :endDate)")
    Long countSalesBetween(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
                           @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.active = true AND s.status.status <> 'DELETED' " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR s.date >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR s.date <= :endDate)")
    java.math.BigDecimal sumRevenueBetween(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
                                           @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("SELECT s.status.status, COUNT(s) FROM Sale s WHERE s.active = true GROUP BY s.status.status")
    java.util.List<Object[]> countSalesByStatus();
}
