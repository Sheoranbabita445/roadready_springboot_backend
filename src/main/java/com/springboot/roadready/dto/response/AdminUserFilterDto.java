package com.springboot.roadready.dto.response;

import java.util.List;

public record AdminUserFilterDto(
        List<AdminUserResponseDto> list,
        PaginationDto paginationInfo
) {
}
