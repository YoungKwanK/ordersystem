package com.beyond16.ordersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SSEMessageDto {
    private String sender;
    private String receiver;
    private Long orderingId;
}
