package com.springboot.roadready.mapper;


import com.springboot.roadready.dto.request.RentalAgentRequestDto;
import com.springboot.roadready.model.RentalAgent;
import org.springframework.stereotype.Component;

@Component
public class RentalAgentMapper {

    public static RentalAgent convertDtoToEntity(RentalAgentRequestDto dto) {
        RentalAgent rentalAgent = new RentalAgent();
        rentalAgent.setEmployeeCode(dto.employeeCode());
        return rentalAgent;
    }
}
