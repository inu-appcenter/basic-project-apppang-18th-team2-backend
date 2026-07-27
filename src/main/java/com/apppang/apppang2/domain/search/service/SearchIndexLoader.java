package com.apppang.apppang2.domain.search.service;

import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//배포 시 전체 상품명을 Redis 자동완성 색인에 적재
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexLoader implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        //기존 색인을 비우고 현재 DB 기준으로 재적재
        redisTemplate.delete(SearchService.AUTOCOMPLETE_KEY);

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            log.info("자동완성 색인: 적재할 상품이 없습니다.");
            return;
        }

        //소문자이름<구분자>원본이름, 점수는 사용하지 않으므로 0
        Set<ZSetOperations.TypedTuple<String>> tuples = products.stream()
                .map(p -> ZSetOperations.TypedTuple.of(
                        p.getName().toLowerCase() + SearchService.SEPARATOR + p.getName(), 0.0))
                .collect(Collectors.toSet());

        redisTemplate.opsForZSet().add(SearchService.AUTOCOMPLETE_KEY, tuples);
        log.info("자동완성 색인: 상품 {}건 적재 완료", products.size());
        //상품 추가가 없으므로 배포 시에만 색인 업데이트
    }
}
