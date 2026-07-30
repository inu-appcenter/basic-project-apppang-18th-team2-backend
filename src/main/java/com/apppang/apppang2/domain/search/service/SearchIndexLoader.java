package com.apppang.apppang2.domain.search.service;

import com.apppang.apppang2.domain.product.entity.Product;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//배포 시 전체 상품명을 Redis 자동완성 색인에 적재
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexLoader implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {

        try {
            //기존 색인을 비우고 현재 DB 기준으로 재적재
            redisTemplate.delete(SearchService.AUTOCOMPLETE_KEY);

            List<Product> products = productRepository.findAll();
            if (products.isEmpty()) {
                log.info("자동완성 색인: 적재할 상품이 없습니다.");
                return;
            }

            //상품명을 토큰 단위로 쪼개, 각 단어에서 시작하는 조각을 모두 색인
            //상품명에 키워드가 들어간 단어를 모두 조회
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
            for (Product p : products) {
                String name = p.getName();
                String[] words = name.split(" ");
                for (int i = 0; i < words.length; i++) {
                    String suffix = String.join(" ", Arrays.copyOfRange(words, i, words.length));
                    //검색용 조각(소문자) + 구분자 + 표시용 원본명 — 어느 조각이 매칭돼도 원본명이 보인다
                    tuples.add(ZSetOperations.TypedTuple.of(
                            suffix.toLowerCase() + SearchService.SEPARATOR + name, 0.0));
                }
            }

            redisTemplate.opsForZSet().add(SearchService.AUTOCOMPLETE_KEY, tuples);
            log.info("자동완성 색인: 상품 {}건 → 색인 {}개 적재 완료", products.size(), tuples.size());
            //상품 추가가 없으므로 배포 시에만 색인 업데이트

        } catch (DataAccessException e) {
            //색인 실패로 앱 부팅까지 막지 않는다 — 자동완성만 빈 결과가 되고 서비스는 정상 기동
            log.error("자동완성 색인 적재 실패 — Redis 연결 확인 필요", e);
        }
    }
}