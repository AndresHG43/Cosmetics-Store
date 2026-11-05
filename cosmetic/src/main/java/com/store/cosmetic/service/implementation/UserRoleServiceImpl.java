package com.store.cosmetic.service.implementation;

import com.store.cosmetic.config.DateTimeConfig;
import com.store.cosmetic.domain.dto.request.UserRoleCreateRequest;
import com.store.cosmetic.domain.dto.response.UserRoleResponse;
import com.store.cosmetic.domain.mapper.UserRoleMapper;
import com.store.cosmetic.domain.model.Role;
import com.store.cosmetic.domain.model.User;
import com.store.cosmetic.domain.model.UserRole;
import com.store.cosmetic.exception.ConflictException;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.RoleRepository;
import com.store.cosmetic.repository.UserRepository;
import com.store.cosmetic.repository.UserRoleRepository;
import com.store.cosmetic.service.contract.UserRoleService;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper userRoleMapper;
    private final jakarta.servlet.http.HttpServletRequest httpServletRequest;

    public UserRoleServiceImpl(UserRoleRepository userRoleRepository,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               UserRoleMapper userRoleMapper,
                               jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleMapper = userRoleMapper;
        this.httpServletRequest = httpServletRequest;
    }

    private Long getAuthenticatedUserId() {
        Object userId = httpServletRequest.getAttribute("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @Override
    @Transactional
    public UserRoleResponse assign(UserRoleCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId()));
        Optional<UserRole> existing = userRoleRepository.findByUserIdAndRoleId(request.getUserId(), request.getRoleId());
        if (existing.isPresent() && Boolean.TRUE.equals(existing.get().getActive())) {
            throw new ConflictException("User already has this role assigned");
        }
        Long currentUserId = getAuthenticatedUserId();
        UserRole userRole = existing.orElseGet(() -> UserRole.builder()
                .user(user)
                .role(role)
                .userCreated(currentUserId)
                .build());
        userRole.setActive(true);
        userRole.setUserCreated(currentUserId);
        userRole = userRoleRepository.save(userRole);
        return userRoleMapper.toResponse(userRole);
    }

    @Override
    @Transactional(readOnly = true)
    public UserRoleResponse getById(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserRole not found with id: " + id));
        return userRoleMapper.toResponse(userRole);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserRoleResponse> getAll(Pageable pageable) {
        return PageResponse.from(userRoleRepository.findAll(pageable).map(userRoleMapper::toResponse));
    }

    @Override
    @Transactional
    public void revoke(Long id) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserRole not found with id: " + id));
        userRole.setActive(false);
        userRole.setDateDeleted(LocalDateTime.now(DateTimeConfig.APP_ZONE));
        userRole.setUserDeleted(getAuthenticatedUserId());
        userRoleRepository.save(userRole);
    }
}
