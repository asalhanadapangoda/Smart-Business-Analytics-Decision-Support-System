package com.sbadss.service.impl;

import com.sbadss.dto.UserResponse;
import com.sbadss.entity.Role;
import com.sbadss.entity.User;
import com.sbadss.exception.ResourceNotFoundException;
import com.sbadss.mapper.UserMapper;
import com.sbadss.repository.RoleRepository;
import com.sbadss.repository.UserRepository;
import com.sbadss.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        return userMapper.toResponse(
                userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id))
        );
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, String roleName) {
        log.info("Updating role for user id: {} to {}", id, roleName);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        log.info("Toggling status for user id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(!user.isActive());
        log.info("User {} {} successfully", user.getUsername(), user.isActive() ? "activated" : "deactivated");
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getUsersByBranch(Long branchId) {
        log.info("Fetching users for branch id: {}", branchId);
        return userRepository.findByBranchId(branchId).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
}
