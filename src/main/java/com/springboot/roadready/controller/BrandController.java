package com.springboot.roadready.controller;

import com.springboot.roadready.dto.request.BrandRequestDto;
import com.springboot.roadready.dto.response.BrandResponseDto;
import com.springboot.roadready.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("api/brand")
public class BrandController {

    private final BrandService brandService;

    // add a brand (admin, from the Add Car form)
    @PostMapping("/add")
    public BrandResponseDto addBrand(@Valid @RequestBody BrandRequestDto brandRequestDto) {
        return brandService.addBrand(brandRequestDto);
    }

    // get all brands (dropdowns)
    @GetMapping("/all")
    public List<BrandResponseDto> getAllBrands() {
        return brandService.getAllBrands();
    }
}
