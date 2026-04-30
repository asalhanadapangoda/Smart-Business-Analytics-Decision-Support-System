package com.sbadss.service.impl;

import com.sbadss.dto.CustomerRequest;
import com.sbadss.dto.CustomerResponse;
import com.sbadss.entity.Branch;
import com.sbadss.entity.Customer;
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

    @Override
    public List<CustomerResponse> getCustomersByBranch(Long branchId) {
        List<Customer> customers = branchId != null ? 
                customerRepository.findByBranchId(branchId) : customerRepository.findAll();
        return customers.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest dto) {
        log.info("Creating customer: {}", dto.getName());
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new com.sbadss.exception.ResourceNotFoundException("Branch not found"));

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        customer.setBranch(branch);
        customer.setActive(true);

        return convertToResponse(customerRepository.save(customer));
    }

    private CustomerResponse convertToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .branchId(customer.getBranch().getId())
                .build();
    }
}
