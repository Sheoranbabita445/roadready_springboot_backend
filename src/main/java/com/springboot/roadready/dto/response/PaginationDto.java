package com.springboot.roadready.dto.response;

// Same shape CozyHeaven uses: sent to the frontend next to the list of records
public record PaginationDto(
        int totalPages,
        Long totalRecords,
        Integer numRecordsInPage
) {
}
