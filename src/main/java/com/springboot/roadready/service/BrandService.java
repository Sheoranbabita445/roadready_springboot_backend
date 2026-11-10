
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.BrandRequestDto;
import com.springboot.roadready.dto.response.BrandResponseDto;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.mapper.BrandMapper;
import com.springboot.roadready.model.Brand;
import com.springboot.roadready.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;

    // add a brand
    public BrandResponseDto addBrand(BrandRequestDto brandRequestDto) {

        String name = brandRequestDto.name().trim();

        log.info("Adding new brand: {}", name);

        // brand names are unique
        if (brandRepository.countByNameIgnoreCase(name) > 0) {
            throw new InvalidCallException("Brand \"" + name + "\" already exists");
        }

        Brand brand = BrandMapper.convertDtoToEntity(new BrandRequestDto(name));
        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand added successfully with ID: {}", savedBrand.getId());

        return BrandMapper.convertEntityToDto(savedBrand);
    }

    // get all brands for the dropdowns
    public List<BrandResponseDto> getAllBrands() {

        log.info("Fetching all brands");

        List<BrandResponseDto> brands = brandRepository.getAllBrandsSorted()
                .stream()
                .map(BrandMapper::convertEntityToDto)
                .toList();

        log.info("Successfully fetched {} brands", brands.size());

        return brands;
    }
}
