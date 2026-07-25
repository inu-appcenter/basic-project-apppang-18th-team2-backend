package com.apppang.apppang2.domain.search.service;

import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ProductRepository productRepository;

    public List<String> getAutoComplete(String keyword) {
        //검색어가 비어있으면 에러 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요.");
        }

        List<Product> products = productRepository.findTop10ByNameStartingWithIgnoreCaseOrderByIdDesc(keyword);
        return products.stream()
                .map(Product::getName)
                .collect(Collectors.toList());
    }
}
