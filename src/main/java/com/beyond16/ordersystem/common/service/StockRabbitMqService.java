package com.beyond16.ordersystem.common.service;

import com.beyond16.ordersystem.common.dto.StockRabbitMqDto;
import com.beyond16.ordersystem.product.domain.Product;
import com.beyond16.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StockRabbitMqService {
    
    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;
    
//    rabbitmq에 메시지 발행
    public void publish(Long productId, int productCount){
        StockRabbitMqDto stockRabbitMqDto = StockRabbitMqDto.builder()
                .productId(productId)
                .productCount(productCount)
                .build();

        rabbitTemplate.convertAndSend("stockDecreaseQueue", stockRabbitMqDto);
    }
    
//    rabbitmq에 발행된 메시지를 수신
//    Listener는 단일스레드로 메시지를 처리하므로, 동시성 이슈 발생X
    @RabbitListener(queues = "stockDecreaseQueue")
    @Transactional
    public void subscribe(Message message) throws JsonProcessingException {
        String messageBody = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        StockRabbitMqDto stockRabbitMqDto = objectMapper.readValue(messageBody, StockRabbitMqDto.class);
        Product product = productRepository.findById(stockRabbitMqDto.getProductId()).orElseThrow(()->new IllegalArgumentException("product not found"));
        product.updateStockQuantity(stockRabbitMqDto.getProductCount());
    }
}
