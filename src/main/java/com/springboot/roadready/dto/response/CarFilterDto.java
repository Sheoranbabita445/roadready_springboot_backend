package com.springboot.roadready.dto.response;

import java.util.List;

public record CarFilterDto(
        List<CarResponseDto> list,
        PaginationDto paginationInfo
) {
}
