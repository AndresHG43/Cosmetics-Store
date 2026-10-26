package com.store.cosmetic.repository;

import com.store.cosmetic.domain.model.EntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntryStatusRepository extends JpaRepository<EntryStatus, Long>, JpaSpecificationExecutor<EntryStatus> {
    Optional<EntryStatus> findByStatus(String status);
}
