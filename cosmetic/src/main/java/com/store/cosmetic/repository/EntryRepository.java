package com.store.cosmetic.repository;

import com.store.cosmetic.domain.model.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long>, JpaSpecificationExecutor<Entry> {
    Page<Entry> findByActive(Boolean active, Pageable pageable);
    Optional<Entry> findByIdAndActiveTrue(Long id);

    /**
     * Find the latest is_initial entry for a given product that is in DONE status.
     * Used as the inventory cut baseline.
     */
    @Query("SELECT e FROM Entry e " +
           "JOIN e.status s " +
           "JOIN EntryDetail ed ON ed.entry.id = e.id " +
           "WHERE ed.product.id = :productId " +
           "AND e.isInitial = true " +
           "AND s.status = 'DONE' " +
           "AND e.active = true " +
           "ORDER BY e.date DESC")
    List<Entry> findLatestInitialEntryForProduct(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Entry e WHERE e.active = true AND e.status.status <> 'DELETED'")
    Long countTotalActiveEntries();

    @Query("SELECT COALESCE(SUM(e.total), 0) FROM Entry e WHERE e.active = true AND e.status.status <> 'DELETED'")
    java.math.BigDecimal sumTotalActiveEntryExpenditure();

    @Query("SELECT e.status.status, COUNT(e) FROM Entry e WHERE e.active = true GROUP BY e.status.status")
    List<Object[]> countEntriesByStatus();
}
