package com.beyond16.ordersystem.ordering.controller;

import com.beyond16.ordersystem.common.dto.CommonDto;
import com.beyond16.ordersystem.ordering.domain.Ordering;
import com.beyond16.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond16.ordersystem.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ordering")
public class OrderingController {

    private final OrderingService orderingService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderCreateDto> orderCreateDto) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(orderingService.createConcurrent(orderCreateDto))
                .status_code(HttpStatus.CREATED.value())
                .status_message("주문이 추가되었습니다.")
                .build(),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> findAllOrdering(){
        return new ResponseEntity<>(CommonDto.builder()
                .result(orderingService.findAllOrdering())
                .status_code(HttpStatus.OK.value())
                .status_message("모든 주문 조회 성공")
                .build(),
                HttpStatus.OK);
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(){
        return new ResponseEntity<>(CommonDto.builder()
                .result(orderingService.findAllMyOrders())
                .status_code(HttpStatus.OK.value())
                .status_message("모든 주문 조회 성공")
                .build(),
                HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> orderCancel(@PathVariable Long id) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(orderingService.cancel(id))
                .status_code(HttpStatus.OK.value())
                .status_message("주문 취소 성공")
                .build(),
                HttpStatus.OK);
    }
}
