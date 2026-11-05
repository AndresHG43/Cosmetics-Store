package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.request.UserRoleCreateRequest;
import com.store.cosmetic.domain.dto.response.UserRoleResponse;
import com.store.cosmetic.domain.mapper.UserRoleMapper;
import com.store.cosmetic.domain.model.Role;
import com.store.cosmetic.domain.model.User;
import com.store.cosmetic.domain.model.UserRole;
import com.store.cosmetic.exception.ConflictException;
import com.store.cosmetic.repository.RoleRepository;
import com.store.cosmetic.repository.UserRepository;
import com.store.cosmetic.repository.UserRoleRepository;
import com.store.cosmetic.service.implementation.UserRoleServiceImpl;
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
class UserRoleServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private jakarta.servlet.http.HttpServletRequest httpServletRequest;

    @Spy
    private UserRoleMapper userRoleMapper = new UserRoleMapper();

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private User sampleUser;
    private Role sampleRole;
    private UserRole sampleUserRole;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).name("User1").email("u1@test.com").active(true).build();
        sampleRole = Role.builder().id(2L).name("ROLE_ADMIN").active(true).build();
        sampleUserRole = UserRole.builder()
                .id(5L)
                .user(sampleUser)
                .role(sampleRole)
                .active(true)
                .dateCreated(LocalDateTime.now())
                .build();
    }

    @Test
    void testAssignRole_Success() {
        UserRoleCreateRequest request = UserRoleCreateRequest.builder()
                .userId(1L)
                .roleId(2L)
                .build();

        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(sampleRole));
        when(userRoleRepository.findByUserIdAndRoleId(1L, 2L)).thenReturn(Optional.empty());
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(sampleUserRole);

        UserRoleResponse response = userRoleService.assign(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals(2L, response.getRoleId());
        assertEquals("ROLE_ADMIN", response.getRoleName());
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void testAssignRole_Conflict() {
        UserRoleCreateRequest request = UserRoleCreateRequest.builder()
                .userId(1L)
                .roleId(2L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(sampleRole));
        when(userRoleRepository.findByUserIdAndRoleId(1L, 2L)).thenReturn(Optional.of(sampleUserRole));

        assertThrows(ConflictException.class, () -> userRoleService.assign(request));
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void testRevokeRole_SoftDelete() {
        when(httpServletRequest.getAttribute("userId")).thenReturn(1L);
        when(userRoleRepository.findById(5L)).thenReturn(Optional.of(sampleUserRole));

        userRoleService.revoke(5L);

        assertFalse(sampleUserRole.getActive());
        assertNotNull(sampleUserRole.getDateDeleted());
        assertEquals(1L, sampleUserRole.getUserDeleted());
        verify(userRoleRepository).save(sampleUserRole);
    }

    @Test
    void testGetById_Success() {
        when(userRoleRepository.findById(5L)).thenReturn(Optional.of(sampleUserRole));

        UserRoleResponse response = userRoleService.getById(5L);

        assertNotNull(response);
        assertEquals(5L, response.getId());
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserRole> page = new PageImpl<>(List.of(sampleUserRole), pageable, 1);
        when(userRoleRepository.findAll(pageable)).thenReturn(page);

        PageResponse<UserRoleResponse> response = userRoleService.getAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }
}
