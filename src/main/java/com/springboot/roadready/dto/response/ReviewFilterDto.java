package com.springboot.roadready.dto.response;

import java.util.List;

public record ReviewFilterDto(
        List<ReviewResponseDto> list,
        PaginationDto paginationInfo
) {
}
