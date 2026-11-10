package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.request.LocationRequestDto;
import com.springboot.roadready.dto.response.LocationResponseDto;
import com.springboot.roadready.model.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    // convert DTO to Location entity
    public static Location convertDtoToEntity(LocationRequestDto locationRequestDto) {
        Location location = new Location();
        location.setCity(locationRequestDto.city());
        location.setState(locationRequestDto.state());
        location.setAddress(locationRequestDto.address());
        return location;
    }

    // convert Location entity to DTO
    public static LocationResponseDto convertEntityToDto(Location location) {
        return new LocationResponseDto(
                location.getId(),
                location.getCity(),
                location.getState(),
                location.getAddress()
        );
    }
}
