package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.request.RoleCreateRequest;
import com.store.cosmetic.domain.dto.request.RoleUpdateRequest;
import com.store.cosmetic.domain.dto.response.RoleResponse;
import com.store.cosmetic.domain.mapper.RoleMapper;
import com.store.cosmetic.domain.model.Role;
import com.store.cosmetic.exception.ConflictException;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.RoleRepository;
import com.store.cosmetic.service.implementation.RoleServiceImpl;
import com.store.cosmetic.util.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @Spy
    private RoleMapper roleMapper = new RoleMapper();

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrator role")
                .active(true)
                .dateCreated(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateRole_Success() {
        RoleCreateRequest request = RoleCreateRequest.builder()
                .name("SUPERVISOR")
                .description("Supervisor role")
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);

        when(roleRepository.existsByName("SUPERVISOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(2L);
            return r;
        });

        RoleResponse response = roleService.create(request);

        assertNotNull(response);
        assertEquals("SUPERVISOR", response.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void testCreateRole_Conflict() {
        RoleCreateRequest request = RoleCreateRequest.builder()
                .name("ADMIN")
                .description("Duplicate role")
                .build();

        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        assertThrows(ConflictException.class, () -> roleService.create(request));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testGetById_Success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(sampleRole));

        RoleResponse response = roleService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ADMIN", response.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getById(99L));
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Role> page = new PageImpl<>(List.of(sampleRole), pageable, 1);
        when(roleRepository.findAll(pageable)).thenReturn(page);

        PageResponse<RoleResponse> response = roleService.getAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testUpdateRole_Success() {
        RoleUpdateRequest request = RoleUpdateRequest.builder()
                .name("ADMIN_UPDATED")
                .description("Updated description")
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(2L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(sampleRole));
        when(roleRepository.existsByName("ADMIN_UPDATED")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = roleService.update(1L, request);

        assertNotNull(response);
        assertEquals("ADMIN_UPDATED", response.getName());
        verify(roleRepository).save(sampleRole);
    }

    @Test
    void testDeleteRole_SoftDelete() {
        when(httpServletRequest.getAttribute("userId")).thenReturn(3L);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(sampleRole));

        roleService.delete(1L);

        assertFalse(sampleRole.getActive());
        assertNotNull(sampleRole.getDateDeleted());
        assertEquals(3L, sampleRole.getUserDeleted());
        verify(roleRepository).save(sampleRole);
    }
}
