package com.sbadss.mapper;

import com.sbadss.dto.CustomerRequest;
import com.sbadss.dto.CustomerResponse;
import com.sbadss.entity.Customer;
import com.sbadss.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) return null;
        
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .branchId(customer.getBranch().getId())
                .build();
    }

    public Customer toEntity(CustomerRequest request, Branch branch) {
        if (request == null) return null;

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setBranch(branch);
        customer.setActive(true);
        return customer;
    }
}
