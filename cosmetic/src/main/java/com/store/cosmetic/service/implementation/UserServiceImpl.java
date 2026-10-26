package com.store.cosmetic.service.implementation;

import com.store.cosmetic.domain.dto.request.UserCreateRequest;
import com.store.cosmetic.domain.dto.request.UserUpdateRequest;
import com.store.cosmetic.domain.dto.response.UserResponse;
import com.store.cosmetic.domain.mapper.UserMapper;
import com.store.cosmetic.domain.model.Role;
import com.store.cosmetic.domain.model.User;
import com.store.cosmetic.domain.model.UserRole;
import com.store.cosmetic.exception.ConflictException;
import com.store.cosmetic.exception.ResourceNotFoundException;
import com.store.cosmetic.repository.RoleRepository;
import com.store.cosmetic.repository.UserRepository;
import com.store.cosmetic.repository.UserRoleRepository;
import com.store.cosmetic.service.contract.UserService;
import com.store.cosmetic.specification.NormalizeSpecification;
import com.store.cosmetic.util.PageResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserRoleRepository userRoleRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email '" + request.getEmail() + "' already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .build();
        user = userRepository.save(user);

        List<UserRole> userRoles = new ArrayList<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
                UserRole userRole = UserRole.builder()
                        .user(user)
                        .role(role)
                        .active(true)
                        .userCreated(user.getId())
                        .build();
                userRoles.add(userRoleRepository.save(userRole));
            }
        }
        return userMapper.toResponse(user, userRoles);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndActiveTrue(id);
        return userMapper.toResponse(user, userRoles);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(String search, Pageable pageable) {
        Specification<User> spec = buildSpec(search);
        Page<UserResponse> page = userRepository.findAll(spec, pageable)
                .map(user -> {
                    List<UserRole> roles = userRoleRepository.findByUserIdAndActiveTrue(user.getId());
                    return userMapper.toResponse(user, roles);
                });
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already in use");
        }
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndActiveTrue(user.getId());
        return userMapper.toResponse(user, userRoles);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        user.setDateDeleted(LocalDateTime.now());
        userRepository.save(user);
    }

    private Specification<User> buildSpec(String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search == null || search.isBlank()) {
                predicates.add(cb.equal(root.get("active"), true));
                return cb.or(predicates.toArray(new Predicate[0]));
            }

            predicates.add(cb.equal(root.get("active"), true));
            predicates.add(NormalizeSpecification.normalizedContains(root.get("name"), search, cb));
            predicates.add(NormalizeSpecification.normalizedContains(root.get("lastname"), search, cb));
            predicates.add(NormalizeSpecification.normalizedContains(root.get("email"), search, cb));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
