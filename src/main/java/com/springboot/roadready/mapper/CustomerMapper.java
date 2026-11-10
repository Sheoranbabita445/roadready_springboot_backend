package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.request.CustomerRequestDto;
import com.springboot.roadready.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    // Converting CustomerRequestDto to Customer entity
    public static Customer convertDtoToEntity(CustomerRequestDto dto) {
        Customer customer = new Customer();
        customer.setFirstName(dto.firstName());
        customer.setLastName(dto.lastName());
        customer.setEmail(dto.email());
        customer.setPhone(dto.phone());
        customer.setAddress(dto.address());
        return customer;
    }
}