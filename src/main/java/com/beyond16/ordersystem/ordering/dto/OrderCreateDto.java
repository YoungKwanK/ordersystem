package com.beyond16.ordersystem.ordering.dto;

import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateDto {
    private Long productId;
    private Integer productCount;
}

