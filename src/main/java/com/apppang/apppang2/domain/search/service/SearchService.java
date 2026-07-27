package com.apppang.apppang2.domain.search.service;

import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    //자동완성 색인이 저장되는 Redis 키
    public static final String AUTOCOMPLETE_KEY = "search:autocomplete";
    //멤버 형식: "소문자이름<구분자>원본이름" — 검색은 소문자로, 표시는 원본으로
    public static final String SEPARATOR = "\u0001";

    private final StringRedisTemplate redisTemplate;

    public List<String> getAutoComplete(String keyword) {
        //검색어가 비어있으면 에러 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요.");
        }

        String prefix = keyword.trim().toLowerCase();

        //ZRANGEBYLEX: 사전순으로 정렬된 집합에서 prefix로 시작하는 구간만 잘라 조회
        //prefix로 시작하는 모든 문자열의 상한선
        Set<String> members = redisTemplate.opsForZSet().rangeByLex(
                AUTOCOMPLETE_KEY,
                Range.closed(prefix, prefix + "\uffff"),
                Limit.limit().count(10));

        if (members == null) {
            return List.of();
        }

        //"소문자<구분자>원본"에서 원본 이름만 추출해 반환
        return members.stream()
                .map(m -> m.substring(m.indexOf(SEPARATOR) + 1))
                .toList();
    }
}
