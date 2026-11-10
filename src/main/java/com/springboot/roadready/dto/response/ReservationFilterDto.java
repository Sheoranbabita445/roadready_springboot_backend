package com.springboot.roadready.dto.response;

import java.util.List;

public record ReservationFilterDto(
        List<ReservationResponseDto> list,
        PaginationDto paginationInfo
) {
}
