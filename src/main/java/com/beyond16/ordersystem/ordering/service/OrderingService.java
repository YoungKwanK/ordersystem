package com.beyond16.ordersystem.ordering.service;

import com.beyond16.ordersystem.common.service.StockInventoryService;
import com.beyond16.ordersystem.common.service.StockRabbitMqService;
import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.member.repository.MemberRepository;
import com.beyond16.ordersystem.ordering.domain.OrderDetail;
import com.beyond16.ordersystem.ordering.domain.Ordering;
import com.beyond16.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond16.ordersystem.ordering.dto.OrderListResDto;
import com.beyond16.ordersystem.ordering.repository.OrderingRepository;
import com.beyond16.ordersystem.product.domain.Product;
import com.beyond16.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitMqService stockRabbitMqService;

    public Long createOrdering(List<OrderCreateDto> orderCreateDtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                ()->new EntityNotFoundException("존재하지 않은 회원입니다.")
        );
        Ordering ordering = Ordering.builder()
                .member(member)
                .orderDetails(new ArrayList<>())
                .build();
        for(OrderCreateDto orderCreateDto : orderCreateDtoList){
            Product product = productRepository.findById(orderCreateDto.getProductId()).orElseThrow(
                    ()->new EntityNotFoundException("해당 상품은 존재하지 않습니다.")
            );
            if(product.getStockQuantity()<orderCreateDto.getProductCount()){
//                예외를 강제 발생시킴으로서, 모두 임시저장사항들을 rollback처리
                throw new IllegalArgumentException("재고 수가 부족합니다.");
            }
//            1. 동시에 접근하는 상황에서 update값의 정확성이 깨지고 갱신이상이 발생할 수 있다
//            2. spring버전이나 mysql 버전에 따라 jpa에서 강제 에러(deadlock)를 유발시켜 대부분의 요청 실패 발생
            product.updateStockQuantity(product.getStockQuantity());

            ordering.getOrderDetails().add(OrderDetail.builder()
                        .product(product)
                        .quantity(product.getStockQuantity())
                        .ordering(ordering)
                        .build());
        }
        return orderingRepository.save(ordering).getId();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED) // 격리레벨을 낮춤으로서, 성능 향상과 lock관련 문제 원천 차단
    public Long createConcurrent(List<OrderCreateDto> orderCreateDtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                ()->new EntityNotFoundException("존재하지 않은 회원입니다.")
        );
        Ordering ordering = Ordering.builder()
                .member(member)
                .orderDetails(new ArrayList<>())
                .build();
        for(OrderCreateDto orderCreateDto : orderCreateDtoList){
            Product product = productRepository.findById(orderCreateDto.getProductId()).orElseThrow(
                    ()->new EntityNotFoundException("해당 상품은 존재하지 않습니다.")
            );
            
//            redis에서 재고수량 확인 및 재고수량 감소 처리
            int newQuantity = stockInventoryService.decreaseStockQuantity(product.getId(), orderCreateDto.getProductCount());
            if(newQuantity<0){
                throw new IllegalArgumentException("재고 부족");
            }

            ordering.getOrderDetails().add(OrderDetail.builder()
                    .product(product)
                    .quantity(orderCreateDto.getProductCount())
                    .ordering(ordering)
                    .build());
//            rdb에 사후 update를 위한 메시지 발행(비동기처리)
            stockRabbitMqService.publish(orderCreateDto.getProductId(), orderCreateDto.getProductCount());
        }
        return orderingRepository.save(ordering).getId();
    }

    @Transactional(readOnly = true)
    public List<OrderListResDto> findAllOrdering(){
        return orderingRepository.findAll().stream()
                .map(a->OrderListResDto.fromEntity(a))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderListResDto> findAllMyOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(
                ()->new EntityNotFoundException("존재하지 않는 회원입니다.")
        );
        return orderingRepository.findAllByMember(member).stream()
                .map(a->OrderListResDto.fromEntity(a))
                .toList();
    }
}
