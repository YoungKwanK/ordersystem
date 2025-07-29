package com.beyond16.ordersystem.product.controller;

import com.beyond16.ordersystem.common.dto.CommonDto;
import com.beyond16.ordersystem.product.dto.ProductCreateDto;
import com.beyond16.ordersystem.product.dto.ProductSearchDto;
import com.beyond16.ordersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@ModelAttribute ProductCreateDto productCreateDto) {
        return new ResponseEntity<>(CommonDto.builder()
                .result(productService.save(productCreateDto))
                .status_code(HttpStatus.CREATED.value())
                .status_message("상품 등록 완료")
                .build(),
                HttpStatus.CREATED);
    }
    
    @Transactional(readOnly = true)
    @GetMapping("/list")
    public ResponseEntity<?> findAllList(@PageableDefault(size = 5, direction = Sort.Direction.DESC) Pageable pageable,
                                         ProductSearchDto productSearchDto){
        return new ResponseEntity<>(CommonDto.builder()
                .result(productService.findAllProduct(pageable, productSearchDto))
                .status_code(HttpStatus.OK.value())
                .status_message("모든상품조회성공")
                .build(),
                HttpStatus.OK);
    }
    
    @Transactional(readOnly = true)
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findProductById(@PathVariable Long id){
        return new ResponseEntity<>(CommonDto.builder()
                .result(productService.findProductById(id))
                .status_code(HttpStatus.OK.value())
                .status_message("상세상품조회성공")
                .build(),
                HttpStatus.OK);
    }
}
