package com.sbadss.service.impl;

import com.sbadss.dto.CustomerRequest;
import com.sbadss.dto.CustomerResponse;
import com.sbadss.entity.Branch;
import com.sbadss.entity.Customer;
import com.sbadss.mapper.CustomerMapper;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.CustomerRepository;
import com.sbadss.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerResponse> getCustomersByBranch(Long branchId) {
        List<Customer> customers = branchId != null ? 
                customerRepository.findByBranchId(branchId) : customerRepository.findAll();
        return customers.stream().map(customerMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest dto) {
        log.info("Creating customer: {}", dto.getName());
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Branch not found"));
        
        Customer customer = customerMapper.toEntity(dto, branch);
        return customerMapper.toResponse(customerRepository.save(customer));
    }
}
