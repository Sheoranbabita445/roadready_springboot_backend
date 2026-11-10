package com.springboot.roadready.dto.response;

import java.util.List;

public record PaymentFilterDto(
        List<PaymentResponseDto> list,
        PaginationDto paginationInfo
) {
}
