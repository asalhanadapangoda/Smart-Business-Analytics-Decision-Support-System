package com.sbadss.service.impl;

import com.sbadss.dto.BranchRequest;
import com.sbadss.dto.BranchResponse;
import com.sbadss.entity.Branch;
import com.sbadss.exception.BusinessException;
import com.sbadss.exception.ResourceNotFoundException;
import com.sbadss.mapper.BranchMapper;
import com.sbadss.repository.BranchRepository;
import com.sbadss.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    public List<BranchResponse> getAllBranches() {
        log.info("Fetching all branches");
        return branchRepository.findAll().stream()
                .map(branchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BranchResponse getBranchById(Long id) {
        log.info("Fetching branch by id: {}", id);
        return branchMapper.toResponse(
                branchRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id))
        );
    }

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        log.info("Creating new branch: {}", request.getName());
        if (branchRepository.existsByName(request.getName())) {
            throw new BusinessException("Branch with name '" + request.getName() + "' already exists");
        }
        Branch branch = branchMapper.toEntity(request);
        
        // Auto-generate branchCode if not provided
        if (branch.getBranchCode() == null || branch.getBranchCode().isBlank()) {
            long count = branchRepository.count() + 1;
            String code = String.format("BR-%03d", count);
            branch.setBranchCode(code);
        }
        
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, BranchRequest request) {
        log.info("Updating branch id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        branch.setName(request.getName());
        branch.setLocation(request.getLocation());
        branch.setContactNumber(request.getContactNumber());
        if (request.getTaxRate() != null) {
            branch.setTaxRate(request.getTaxRate());
        }
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deactivateBranch(Long id) {
        log.info("Deactivating branch id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        branch.setActive(false);
        branchRepository.save(branch);
    }
}
