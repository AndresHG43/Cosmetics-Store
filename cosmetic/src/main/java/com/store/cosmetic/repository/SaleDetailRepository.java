package com.store.cosmetic.repository;

import com.store.cosmetic.domain.model.SaleDetail;
import com.store.cosmetic.domain.model.SaleDetailId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, SaleDetailId>, JpaSpecificationExecutor<SaleDetail> {
    @Query(value = "SELECT nextval('sale_detail_sequence')", nativeQuery = true)
    Long getNextDetailSequenceId();

    List<SaleDetail> findBySaleId(Long saleId);
    Page<SaleDetail> findBySaleId(Long saleId, Pageable pageable);

    /**
     * Sum of sold quantities for a product after a cutoff date (for sales strictly in PENDING DELIVERY or DONE).
     */
    @Query("SELECT COALESCE(SUM(sd.quantity), 0) FROM SaleDetail sd " +
           "JOIN sd.sale s " +
           "WHERE sd.product.id = :productId " +
           "AND s.date > :cutoffDate " +
           "AND s.status.status IN ('PENDING DELIVERY', 'DONE') " +
           "AND sd.active = true")
    Integer sumQuantityAfterCutoff(@Param("productId") Long productId, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Sum of all active sold quantities for a product strictly in PENDING DELIVERY or DONE (no cutoff).
     */
    @Query("SELECT COALESCE(SUM(sd.quantity), 0) FROM SaleDetail sd " +
           "JOIN sd.sale s " +
           "WHERE sd.product.id = :productId " +
           "AND s.status.status IN ('PENDING DELIVERY', 'DONE') " +
           "AND sd.active = true")
    Integer sumAllActiveQuantity(@Param("productId") Long productId);

    /**
     * Get the quantity from a specific entry for a specific product (initial cut quantity).
     */
    @Query("SELECT COALESCE(SUM(sd.quantity), 0) FROM SaleDetail sd " +
           "WHERE sd.product.id = :productId AND sd.sale.id = :saleId AND sd.active = true")
    Integer sumQuantityByEntryAndProduct(@Param("productId") Long productId, @Param("saleId") Long saleId);

    /**
     * Top-selling cosmetic products ordered by total quantity sold.
     */
    @Query(value = "SELECT sd.product.id, sd.product.name, SUM(sd.quantity), SUM(sd.priceSale * sd.quantity) " +
           "FROM SaleDetail sd " +
           "JOIN sd.sale s " +
           "WHERE sd.active = true AND s.active = true AND s.status.status <> 'DELETED' " +
           "GROUP BY sd.product.id, sd.product.name " +
           "ORDER BY SUM(sd.quantity) DESC",
           countQuery = "SELECT COUNT(DISTINCT sd.product.id) FROM SaleDetail sd JOIN sd.sale s WHERE sd.active = true AND s.active = true AND s.status.status <> 'DELETED'")
    Page<Object[]> findTopSellingProducts(Pageable pageable);
}
