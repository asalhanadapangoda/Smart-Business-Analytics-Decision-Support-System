package com.sbadss.service;

import com.sbadss.dto.BranchRequest;
import com.sbadss.dto.BranchResponse;

import java.util.List;

public interface BranchService {
    List<BranchResponse> getAllBranches();
    BranchResponse getBranchById(Long id);
    BranchResponse createBranch(BranchRequest request);
    BranchResponse updateBranch(Long id, BranchRequest request);
    void deactivateBranch(Long id);
}
