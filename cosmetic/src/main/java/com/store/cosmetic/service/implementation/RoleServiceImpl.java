package com.store.cosmetic.service.implementation;

import com.store.cosmetic.domain.dto.request.RoleCreateRequest;
import com.store.cosmetic.domain.dto.request.RoleUpdateRequest;
import com.store.cosmetic.domain.dto.response.RoleResponse;
import com.store.cosmetic.domain.mapper.RoleMapper;
import com.store.cosmetic.domain.model.Role;
import com.store.cosmetic.exception.ConflictException;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.RoleRepository;
import com.store.cosmetic.service.contract.RoleService;
import com.store.cosmetic.util.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final jakarta.servlet.http.HttpServletRequest httpServletRequest;

    public RoleServiceImpl(RoleRepository roleRepository,
                           RoleMapper roleMapper,
                           jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
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
    public RoleResponse create(RoleCreateRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ConflictException("Role with name '" + request.getName() + "' already exists");
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .userCreated(getAuthenticatedUserId())
                .build();
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getAll(Pageable pageable) {
        return PageResponse.from(roleRepository.findAll(pageable).map(roleMapper::toResponse));
    }

    @Override
    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        if (!role.getName().equals(request.getName()) && roleRepository.existsByName(request.getName())) {
            throw new ConflictException("Role with name '" + request.getName() + "' already exists");
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setUserUpdated(getAuthenticatedUserId());
        role = roleRepository.save(role);
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        role.setActive(false);
        role.setDateDeleted(LocalDateTime.now());
        role.setUserDeleted(getAuthenticatedUserId());
        roleRepository.save(role);
    }
}
