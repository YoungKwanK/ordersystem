package com.beyond16.ordersystem.product.service;

import com.beyond16.ordersystem.member.domain.Member;
import com.beyond16.ordersystem.member.repository.MemberRepository;
import com.beyond16.ordersystem.product.domain.Product;
import com.beyond16.ordersystem.product.dto.ProductCreateDto;
import com.beyond16.ordersystem.product.dto.ProductResDto;
import com.beyond16.ordersystem.product.dto.ProductSearchDto;
import com.beyond16.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;

    public Long save(ProductCreateDto productCreateDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 이메일입니다."));
        Product product = productRepository.save(productCreateDto.toEntity(member));

        if(!productCreateDto.getProductImage().isEmpty()) {
            String fileName = "product-"+product.getId()+"-productImage-"+productCreateDto.getProductImage().getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productCreateDto.getProductImage().getContentType())
                    .build();

            try {
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productCreateDto.getProductImage().getBytes()));
            } catch (Exception e) {
                throw new IllegalArgumentException("이미지 업로드 실패");
            }
//        이미지 url추출
            String imgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImageUrl(imgUrl);
        }
        return product.getId();
    }

    @Transactional(readOnly = true)
    public Page<ProductResDto> findAllProduct(Pageable pageable, ProductSearchDto productSearchDto){
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicateList = new ArrayList<>();
                if(productSearchDto.getCategory()!=null){
                    predicateList.add(cb.equal(root.get("category"), productSearchDto.getCategory()));
                }
                if(productSearchDto.getProductName()!=null){
                    predicateList.add(cb.like(root.get("name"), "%"+productSearchDto.getProductName()+"%"));
                }
                Predicate[] predicateArr = new Predicate[predicateList.size()];
                predicateList.toArray(predicateArr);

                Predicate predicate = cb.and(predicateArr);
                return predicate;
            }
        };

        return productRepository.findAll(specification, pageable)
                .map(a-> ProductResDto.fromEntity(a));
    }

    @Transactional(readOnly = true)
    public ProductResDto findProductById(Long id){
        return ProductResDto.fromEntity(productRepository.findById(id).orElseThrow(
                ()-> new EntityNotFoundException("해당 상품이 존재하지 않습니다.")
        ));
    }
}
