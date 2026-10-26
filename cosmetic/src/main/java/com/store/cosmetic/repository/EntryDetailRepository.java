package com.store.cosmetic.repository;

import com.store.cosmetic.domain.model.EntryDetail;
import com.store.cosmetic.domain.model.EntryDetailId;
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
public interface EntryDetailRepository extends JpaRepository<EntryDetail, EntryDetailId>, JpaSpecificationExecutor<EntryDetail> {
    @Query(value = "SELECT nextval('entry_detail_sequence')", nativeQuery = true)
    Long getNextDetailSequenceId();

    List<EntryDetail> findByEntryId(Long entryId);
    Page<EntryDetail> findByEntryId(Long entryId, Pageable pageable);

    /**
     * Sum of entry quantities for a product after a cutoff date (strictly for entries in DONE status).
     * Excludes the is_initial entry itself (entry.date > cutoff).
     */
    @Query("SELECT COALESCE(SUM(ed.quantity), 0) FROM EntryDetail ed " +
           "JOIN ed.entry e " +
           "WHERE ed.product.id = :productId " +
           "AND e.date > :cutoffDate " +
           "AND e.status.status = 'DONE' " +
           "AND ed.active = true")
    Integer sumQuantityAfterCutoff(@Param("productId") Long productId, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Sum of all active entry quantities for a product strictly in DONE status (no cutoff).
     */
    @Query("SELECT COALESCE(SUM(ed.quantity), 0) FROM EntryDetail ed " +
           "JOIN ed.entry e " +
           "WHERE ed.product.id = :productId " +
           "AND e.status.status = 'DONE' " +
           "AND ed.active = true")
    Integer sumAllActiveQuantity(@Param("productId") Long productId);

    /**
     * Get the quantity in a specific entry for a specific product (initial cut baseline).
     */
    @Query("SELECT COALESCE(SUM(ed.quantity), 0) FROM EntryDetail ed " +
           "WHERE ed.product.id = :productId AND ed.entry.id = :entryId AND ed.active = true")
    Integer sumQuantityByEntryAndProduct(@Param("productId") Long productId, @Param("entryId") Long entryId);
}
