package com.beyond16.ordersystem.product.domain;

import com.beyond16.ordersystem.common.domain.BaseTimeEntity;
import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private String imagePath;

    public void updateImageUrl(String imageUrl) {
        this.imagePath = imageUrl;
    }

    public void updateProduct(ProductUpdateDto productUpdateDto){
        this.name = productUpdateDto.getName();
        this.category = productUpdateDto.getCategory();
        this.price = productUpdateDto.getPrice();
        this.stockQuantity = productUpdateDto.getStockQuantity();
    }

    public void updateStockQuantity(Integer stockQuantity){
        this.stockQuantity -= stockQuantity;
    }

    public void cancelOrder(int orderQuantity){
        this.stockQuantity += orderQuantity;
    }
}
