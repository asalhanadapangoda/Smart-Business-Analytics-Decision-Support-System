package com.sbadss.service;

import com.sbadss.dto.CustomerRequest;
import com.sbadss.dto.CustomerResponse;
import java.util.List;

public interface CustomerService {
    List<CustomerResponse> getCustomersByBranch(Long branchId);
    CustomerResponse createCustomer(CustomerRequest dto);
}
