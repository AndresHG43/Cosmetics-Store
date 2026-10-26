package com.store.cosmetic.repository;

import com.store.cosmetic.domain.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long>, JpaSpecificationExecutor<UserRole> {
    List<UserRole> findByUserIdAndActiveTrue(Long userId);
    Page<UserRole> findByUserId(Long userId, Pageable pageable);
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
}
