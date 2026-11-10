
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.BrandRequestDto;
import com.springboot.roadready.dto.response.BrandResponseDto;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.model.Brand;
import com.springboot.roadready.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @InjectMocks
    private BrandService brandService;

    @Mock
    private BrandRepository brandRepository;


    // Test 1: Add brand successfully
    @Test
    public void addBrandTest() {

        BrandRequestDto dto = new BrandRequestDto("Toyota");

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Toyota");

        when(brandRepository.countByNameIgnoreCase("Toyota"))
                .thenReturn(0L);

        when(brandRepository.save(any(Brand.class)))
                .thenReturn(brand);

        BrandResponseDto result = brandService.addBrand(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Toyota", result.name());

        verify(brandRepository, times(1)).countByNameIgnoreCase("Toyota");
        verify(brandRepository, times(1)).save(any(Brand.class));
        verifyNoMoreInteractions(brandRepository);
    }


    // Test 2: Add brand with extra spaces
    @Test
    public void addBrandTest_TrimName() {

        BrandRequestDto dto = new BrandRequestDto("  Honda  ");

        Brand brand = new Brand();
        brand.setId(2L);
        brand.setName("Honda");

        when(brandRepository.countByNameIgnoreCase("Honda"))
                .thenReturn(0L);

        when(brandRepository.save(any(Brand.class)))
                .thenReturn(brand);

        BrandResponseDto result = brandService.addBrand(dto);

        assertEquals(2L, result.id());
        assertEquals("Honda", result.name());

        verify(brandRepository, times(1)).countByNameIgnoreCase("Honda");
        verify(brandRepository, times(1)).save(any(Brand.class));
        verifyNoMoreInteractions(brandRepository);
    }


    // Test 3: Do not add duplicate brand
    @Test
    public void addBrandTest_DuplicateBrand() {

        BrandRequestDto dto = new BrandRequestDto("Toyota");

        when(brandRepository.countByNameIgnoreCase("Toyota"))
                .thenReturn(1L);

        assertThrows(InvalidCallException.class, () -> {
            brandService.addBrand(dto);
        });

        verify(brandRepository, times(1)).countByNameIgnoreCase("Toyota");
        verifyNoMoreInteractions(brandRepository);
    }


    // Test 4: Get all brands
    @Test
    public void getAllBrandsTest() {

        Brand brand1 = new Brand();
        brand1.setId(1L);
        brand1.setName("Honda");

        Brand brand2 = new Brand();
        brand2.setId(2L);
        brand2.setName("Toyota");

        when(brandRepository.getAllBrandsSorted())
                .thenReturn(List.of(brand1, brand2));

        List<BrandResponseDto> result = brandService.getAllBrands();

        assertEquals(2, result.size());
        assertEquals("Honda", result.get(0).name());
        assertEquals("Toyota", result.get(1).name());

        verify(brandRepository, times(1)).getAllBrandsSorted();
        verifyNoMoreInteractions(brandRepository);
    }


    // Test 5: Get all brands when no brands exist
    @Test
    public void getAllBrandsTest_Empty() {

        when(brandRepository.getAllBrandsSorted())
                .thenReturn(List.of());

        List<BrandResponseDto> result = brandService.getAllBrands();

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(brandRepository, times(1)).getAllBrandsSorted();
        verifyNoMoreInteractions(brandRepository);
    }
}
