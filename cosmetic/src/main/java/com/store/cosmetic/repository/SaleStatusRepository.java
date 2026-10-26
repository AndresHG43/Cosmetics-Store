package com.store.cosmetic.repository;

import com.store.cosmetic.domain.model.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaleStatusRepository extends JpaRepository<SaleStatus, Long>, JpaSpecificationExecutor<SaleStatus> {
    Optional<SaleStatus> findByStatus(String status);
}
