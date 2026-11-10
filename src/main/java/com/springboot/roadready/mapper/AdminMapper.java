package com.springboot.roadready.mapper;

import com.springboot.roadready.dto.request.AdminRequestDto;
import com.springboot.roadready.dto.response.AdminUserResponseDto;
import com.springboot.roadready.model.Admin;
import com.springboot.roadready.model.User;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {
    //converting admindto to admin
    public static Admin convertDtoToEntity(AdminRequestDto dto) {
        Admin admin = new Admin();
        admin.setName(dto.name());
        admin.setPhone(dto.phone());
        return admin;
    }


    // convert User entity to AdminUserResponseDto
    public static AdminUserResponseDto convertUserToDto(User user) {
        return new AdminUserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isActive()
        );
    }
}