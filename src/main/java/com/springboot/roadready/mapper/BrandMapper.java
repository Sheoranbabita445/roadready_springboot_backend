package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.request.BrandRequestDto;
import com.springboot.roadready.dto.response.BrandResponseDto;
import com.springboot.roadready.model.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

    // convert DTO to Brand entity
    public static Brand convertDtoToEntity(BrandRequestDto brandRequestDto) {
        Brand brand = new Brand();
        brand.setName(brandRequestDto.name());
        return brand;
    }

    // convert Brand entity to DTO
    public static BrandResponseDto convertEntityToDto(Brand brand) {
        return new BrandResponseDto(
                brand.getId(),
                brand.getName()
        );
    }
}
