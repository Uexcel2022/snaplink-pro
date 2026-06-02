package com.uexcel.snaplinkpro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaginationMeta{
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

}
