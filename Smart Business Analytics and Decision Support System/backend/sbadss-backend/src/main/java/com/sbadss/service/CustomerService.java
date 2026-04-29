package com.sbadss.service;

import com.sbadss.dto.CustomerDTO;
import com.sbadss.entity.Branch;
import com.sbadss.entity.Customer;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;

    public List<CustomerDTO> getCustomersByBranch(Long branchId) {
        List<Customer> customers = branchId != null ? 
                customerRepository.findByBranchId(branchId) : customerRepository.findAll();
        return customers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public CustomerDTO createCustomer(CustomerDTO dto) {
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        customer.setBranch(branch);
        customer.setActive(true);

        return convertToDTO(customerRepository.save(customer));
    }

    private CustomerDTO convertToDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .branchId(customer.getBranch().getId())
                .build();
    }
}
