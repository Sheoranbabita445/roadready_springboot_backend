
package com.springboot.roadready.service;

import com.springboot.roadready.dto.request.CarRequestDto;
import com.springboot.roadready.dto.response.CarFilterDto;
import com.springboot.roadready.dto.response.CarResponseDto;
import com.springboot.roadready.dto.response.PaginationDto;
import com.springboot.roadready.enums.CarStatus;
import com.springboot.roadready.exception.InvalidCallException;
import com.springboot.roadready.exception.ResourceNotFoundException;
import com.springboot.roadready.mapper.CarMapper;
import com.springboot.roadready.model.Brand;
import com.springboot.roadready.model.Car;
import com.springboot.roadready.model.Location;
import com.springboot.roadready.model.RentalAgent;
import com.springboot.roadready.repository.BrandRepository;
import com.springboot.roadready.repository.CarRepository;
import com.springboot.roadready.repository.LocationRepository;
import com.springboot.roadready.repository.RentalAgentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarService {

    private final CarRepository carRepository;
    private final BrandRepository brandRepository;
    private final LocationRepository locationRepository;
    private final RentalAgentRepository rentalAgentRepository;

    private final static String uploadDir =
            "C:\\Users\\Krish\\Downloads\\RoadReady-Frontend-Updated (2)\\roadready_frontend\\public\\images\\cars";
    private final static Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    // add a car
    @Transactional
    public Long addCar(CarRequestDto carRequestDto) {

        log.info("Adding a new car with registration number: {}",
                carRequestDto.registrationNumber());

        // Get brand
        Brand brand = brandRepository.findById(carRequestDto.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        // Get location
        Location location = locationRepository.findById(carRequestDto.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        // Get rental agent
        RentalAgent rentalAgent = rentalAgentRepository.findById(carRequestDto.rentalAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Rental agent not found"));

        // Convert DTO to Car entity
        Car car = convertCar(carRequestDto);

        // Set car relationships
        car.setBrand(brand);
        car.setLocation(location);
        car.setRentalAgent(rentalAgent);

        // Save car
        try {
            Long carId = carRepository.saveAndFlush(car).getId();

            log.info("Car added successfully with ID: {}", carId);

            return carId;
        } catch (DataIntegrityViolationException e) {
            throw new InvalidCallException("A car with this registration number already exists");
        }
    }


    // get all cars
    public CarFilterDto getAllCars(Integer page, Integer size) {

        log.info("Fetching all cars. Page: {}, Size: {}", page, size);

        return dynamicFilterCar(null, null, null, null, null, null, null, null, null, page, size);
    }


    // get a car by its ID
    public CarResponseDto getCarById(Long id) {

        log.info("Fetching car with ID: {}", id);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));

        log.info("Car fetched successfully with ID: {}", id);

        return CarMapper.convertEntityToDto(car);
    }


    // update car details
    @Transactional
    public void updateCar(Long carId, CarRequestDto carRequestDto) {

        log.info("Updating car details for car ID: {}", carId);

        // Get car
        Car car = carRepository.findById(carId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Car not found"));

        // Get brand
        Brand brand = brandRepository.findById(carRequestDto.brandId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Brand not found"));

        // Get location
        Location location = locationRepository.findById(carRequestDto.locationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Location not found"));

        // Get rental agent
        RentalAgent rentalAgent = rentalAgentRepository.findById(carRequestDto.rentalAgentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Rental agent not found"));

        // Update car details
        try {
            CarMapper.updateEntityFromDto(carRequestDto, car);
        } catch (IllegalArgumentException e) {
            throw new InvalidCallException("Invalid car status");
        }

        // Update car relationships
        car.setBrand(brand);
        car.setLocation(location);
        car.setRentalAgent(rentalAgent);

        // Save updated car
        try {
            carRepository.saveAndFlush(car);

            log.info("Car details updated successfully for car ID: {}", carId);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidCallException("A car with this registration number already exists");
        }
    }


    // filter cars
    public CarFilterDto dynamicFilterCar(
            Long brandId,
            Long locationId,
            String fuelType,
            String transmission,
            String vehicleType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CarStatus status,
            String search,
            Integer page,
            Integer size
    ) {

        log.info("Filtering cars. Brand ID: {}, Location ID: {}, Status: {}, Page: {}, Size: {}",
                brandId, locationId, status, page, size);

        // price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidCallException("Minimum price cannot be more than maximum price");
        }

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // blank text from the query string means "no filter"
        fuelType = (fuelType == null || fuelType.isBlank()) ? null : fuelType.trim();
        transmission = (transmission == null || transmission.isBlank()) ? null : transmission.trim();
        vehicleType = (vehicleType == null || vehicleType.isBlank()) ? null : vehicleType.trim();

        // search text becomes a lower-case LIKE pattern (null = no search)
        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.trim().toLowerCase() + "%";

        // filter and page in the database
        Page<CarResponseDto> carPage = carRepository.dynamicFilterCar(
                brandId,
                locationId,
                fuelType,
                transmission,
                vehicleType,
                minPrice,
                maxPrice,
                status,
                searchPattern,
                pageable
        );

        log.info("Car filtering completed successfully. Records found: {}",
                carPage.getContent().size());

        return new CarFilterDto(
                carPage.getContent(),
                new PaginationDto(carPage.getTotalPages(), carPage.getTotalElements(), carPage.getContent().size())
        );
    }


    // get cars assigned to the logged-in rental agent
    public CarFilterDto getCarsByRentalAgentUsername(
            String username,
            CarStatus status,
            String search,
            Integer page,
            Integer size) {

        log.info("Fetching cars for rental agent: {}. Status: {}, Page: {}, Size: {}",
                username, status, page, size);

        // validate pagination
        if (page == null || size == null || page < 0 || size < 1 || size > 100)
            throw new InvalidCallException("Page must be 0 or above and size must be between 1 and 100");

        // prepare pagination
        Pageable pageable = PageRequest.of(page, size);

        // search text becomes a lower-case LIKE pattern (null = no search)
        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.trim().toLowerCase() + "%";

        Page<CarResponseDto> carPage = carRepository.getCarsByRentalAgentUsername(
                username, status, searchPattern, pageable);

        log.info("Successfully fetched {} cars for rental agent: {}",
                carPage.getContent().size(), username);

        return new CarFilterDto(
                carPage.getContent(),
                new PaginationDto(carPage.getTotalPages(), carPage.getTotalElements(), carPage.getContent().size())
        );
    }


    // update the status of a car assigned to the logged-in rental agent
    @Transactional
    public String updateCarStatusByRentalAgent(
            Long carId,
            CarStatus status,
            String username) {

        log.info("Rental agent {} updating car ID: {} status to: {}",
                username, carId, status);

        // Update the car status
        int updatedRows = carRepository.updateCarStatusByRentalAgent(
                carId, username, status);

        // Check whether the car was found and assigned to the rental agent
        if (updatedRows == 0) {
            throw new ResourceNotFoundException(
                    "Car not found or not assigned to this rental agent");
        }

        log.info("Car status updated successfully for car ID: {}", carId);

        // Return success message
        return "Car status updated successfully";
    }


    // update maintenance details for a car assigned to a rental agent
    @Transactional
    public void updateMaintenanceNoteByRentalAgent(
            Long carId,
            String maintenanceNote,
            String username) {

        log.info("Rental agent {} updating maintenance note for car ID: {}",
                username, carId);

        // Update the maintenance note
        int updatedRows = carRepository.updateMaintenanceNoteByRentalAgent(
                carId, username, maintenanceNote);

        // Check whether the car was found and assigned to the rental agent
        if (updatedRows == 0) {
            throw new ResourceNotFoundException(
                    "Car not found or not assigned to this rental agent");
        }

        log.info("Maintenance note updated successfully for car ID: {}", carId);
    }


    // store the picture
    // The file is copied to the upload folder and its file name is saved in the car's imageUrl.
    @Transactional
    public CarResponseDto uploadImage(Long carId, MultipartFile file) throws IOException {

        log.info("Uploading image for car ID: {}", carId);

        // Fetch car
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found"));

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new InvalidCallException("Please choose an image file to upload");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidCallException("Only image files can be uploaded");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new InvalidCallException("Allowed image types are JPG, PNG, WEBP and GIF");
        }

        // Generate a unique, safe file name (no folders, no odd characters)
        String baseName = Paths.get(originalName).getFileName().toString()
                .replaceAll("[^A-Za-z0-9._-]", "_");
        String fileName = System.currentTimeMillis() + "_" + baseName;

        // Copy file to the upload folder
        Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        Path target = directory.resolve(fileName).normalize();
        if (!target.startsWith(directory)) {
            throw new InvalidCallException("Invalid file name");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // Save the file name in the database
        car.setImageUrl(fileName);
        carRepository.save(car);

        log.info("Car image uploaded successfully for car ID: {}", carId);

        return CarMapper.convertEntityToDto(car);
    }


    // convert the request into a Car, turning a bad status text into a clear error
    private Car convertCar(CarRequestDto dto) {
        try {
            return CarMapper.convertDtoToEntity(dto);
        } catch (IllegalArgumentException e) {
            throw new InvalidCallException("Invalid car status");
        }
    }
}
