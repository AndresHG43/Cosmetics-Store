package com.store.cosmetic.service;

import com.store.cosmetic.domain.dto.request.UserCreateRequest;
import com.store.cosmetic.domain.dto.request.UserUpdateRequest;
import com.store.cosmetic.domain.dto.response.UserResponse;
import com.store.cosmetic.domain.mapper.RoleMapper;
import com.store.cosmetic.domain.mapper.UserMapper;
import com.store.cosmetic.domain.model.Role;
import com.store.cosmetic.domain.model.User;
import com.store.cosmetic.domain.model.UserRole;
import com.store.cosmetic.exception.ConflictException;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.RoleRepository;
import com.store.cosmetic.repository.UserRepository;
import com.store.cosmetic.repository.UserRoleRepository;
import com.store.cosmetic.service.implementation.UserServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Spy
    private UserMapper userMapper = new UserMapper(new RoleMapper());

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .description("Administrator")
                .active(true)
                .build();

        sampleUser = User.builder()
                .id(1L)
                .name("Maria")
                .lastname("Perez")
                .email("maria@example.com")
                .password("$2a$10$encoded")
                .active(true)
                .dateCreated(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Maria")
                .lastname("Perez")
                .email("maria@example.com")
                .password("plainPass123")
                .roleIds(List.of(1L))
                .build();

        when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(sampleRole));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertEquals("Maria", response.getName());
        assertEquals("maria@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateEmailConflict() {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("Maria")
                .lastname("Perez")
                .email("maria@example.com")
                .password("plainPass123")
                .build();

        when(userRepository.existsByEmail("maria@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Maria", response.getName());
    }

    @Test
    void testGetById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(sampleUser), pageable, 1);
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<UserResponse> response = userService.getAll("Maria", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testUpdateUser_Success() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("Maria Elena")
                .lastname("Perez")
                .email("maria.elena@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmail("maria.elena@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.update(1L, request);

        assertNotNull(response);
        verify(userRepository).save(sampleUser);
    }

    @Test
    void testDeleteUser_SoftDelete() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        userService.delete(1L);

        assertFalse(sampleUser.getActive());
        assertNotNull(sampleUser.getDateDeleted());
        verify(userRepository).save(sampleUser);
    }
}
