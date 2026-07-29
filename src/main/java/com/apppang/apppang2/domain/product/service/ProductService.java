package com.apppang.apppang2.domain.product.service;

import com.apppang.apppang2.domain.product.dto.response.ProductDetailResponse;
import com.apppang.apppang2.domain.product.dto.response.ProductListResponse;
import com.apppang.apppang2.domain.product.dto.response.ProductResponse;
import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.domain.search.service.SearchService;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int PAGE_SIZE = 30;    //조회 개수 30개 고정

    private final ProductRepository productRepository;
    private final SearchService searchService;

    //상품 목록 조회 로직
    public ProductListResponse getProducts(String keyword, Long categoryId,
                                           boolean discountOnly, String event, String sort, int page){
        if (page < 0){
            throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 조회 조건입니다.");
        }

        //검색 실행 시 인기 검색어 점수 +1 (키워드 없는 목록 조회는 반영 X)
        if (page == 0 && keyword != null && !keyword.trim().isEmpty()) {
            searchService.recordKeyword(keyword);
        }


        Pageable pageable = PageRequest.of(page, PAGE_SIZE, toSort(sort)); //각 페이지에 30개 씩 조회
        //필터+정렬+페이징이 적용된 상품 30개(이하)를 레포지토리에서 호출
        Slice<Product> slice = productRepository.search(keyword, categoryId, discountOnly, event, pageable);
        //Slice에서 상품 리스트 꺼내고, 각 product를 ProductResponse(product)로 변환 결과를 리스트에 추가
        List<ProductResponse> products = slice.getContent().stream()
                .map(ProductResponse::new)
                .toList();

        return new ProductListResponse(products, page, slice.hasNext());
    }

    //정렬 파라미터 문자열을 JPA Sort로 변환, 허용된 값이 아니면 400
    private Sort toSort(String sort){
        if (sort == null) return Sort.by("createdAt").descending();     //기본값: 최신순
        return switch (sort){
            case "latest" -> Sort.by("createdAt").descending();
            case "rating" -> Sort.by("ratingAvg").descending();
            //가격 정렬은 실제 판매가(할인가 있으면 할인가, 없으면 정가) 기준
            case "priceAsc" -> Sort.by("salePrice").ascending();
            case "priceDesc" -> Sort.by("salePrice").descending();
            // 허용 외 값 오류처리
            default -> throw new CustomException(HttpStatus.BAD_REQUEST, "잘못된 조회 조건입니다.");
        };
    }


    //상품 상세 목록 조회 로직
    public ProductDetailResponse getProduct(Long productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

        return new ProductDetailResponse(product);
    }
}