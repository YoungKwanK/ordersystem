package com.beyond16.ordersystem.ordering.dto;

import com.beyond16.ordersystem.ordering.domain.OrderDetail;
import com.beyond16.ordersystem.ordering.domain.OrderStatus;
import com.beyond16.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderListResDto {
    private Long orderId;
    private OrderStatus orderStatus;
    private List<OrderDetailDto> orderDetails;

    public static OrderListResDto fromEntity(Ordering ordering){
        List<OrderDetailDto> orderDetailsList = new ArrayList<>();
        for(OrderDetail orderDetail : ordering.getOrderDetails()){
            orderDetailsList.add(OrderDetailDto.fromEntity(orderDetail));
        }
        return OrderListResDto.builder()
                .orderId(ordering.getId())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(orderDetailsList)
                .build();
    }
}
