package com.apppang.apppang2.domain.product.controller;

import com.apppang.apppang2.domain.product.dto.response.ProductDetailResponse;
import com.apppang.apppang2.domain.product.dto.response.ProductListResponse;
import com.apppang.apppang2.domain.product.service.ProductService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PRODUCT")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회")
    @GetMapping("/api/products")
    public ApiResponse<ProductListResponse> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean discountOnly,
            @RequestParam(name = "event", required = false) String event,
            @RequestParam(required = false) String sort,
            @RequestParam int page      //필수 파라미터, 없으면 400
    ){
        ProductListResponse response = productService.getProducts(keyword, categoryId, discountOnly, event, sort, page);

        return ApiResponse.success("상품 목록 조회에 성공했습니다.", response);
    }

    @Operation(summary = "상품 상세 조회")
    @GetMapping("/api/products/{productId}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable Long productId){
        ProductDetailResponse response = productService.getProduct(productId);

        return ApiResponse.success("상품 상세 조회에 성공했습니다.", response);
    }
}