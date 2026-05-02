package com.sbadss.mapper;

import com.sbadss.dto.BranchRequest;
import com.sbadss.dto.BranchResponse;
import com.sbadss.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        if (branch == null) return null;
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .branchCode(branch.getBranchCode())
                .location(branch.getLocation())
                .contactNumber(branch.getContactNumber())
                .taxRate(branch.getTaxRate())
                .active(branch.isActive())
                .build();
    }

    public Branch toEntity(BranchRequest request) {
        if (request == null) return null;
        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setBranchCode(request.getBranchCode());
        branch.setLocation(request.getLocation());
        branch.setContactNumber(request.getContactNumber());
        branch.setTaxRate(request.getTaxRate() != null ? request.getTaxRate() : java.math.BigDecimal.ZERO);
        branch.setActive(true);
        return branch;
    }
}
