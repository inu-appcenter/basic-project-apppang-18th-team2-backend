package com.apppang.apppang2.domain.search.service;

import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    //자동완성 색인이 저장되는 Redis 키
    public static final String AUTOCOMPLETE_KEY = "search:autocomplete";
    public static final String POPULAR_KEY = "search:popular";
    //멤버 형식: "소문자이름<구분자>원본이름" — 검색은 소문자로, 표시는 원본으로
    public static final String SEPARATOR = "\u0001";

    private final StringRedisTemplate redisTemplate;

    public List<String> getAutoComplete(String keyword) {
        //검색어가 비어있으면 에러 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요.");
        }

        String prefix = keyword.trim().toLowerCase();

        //100자 넘는 검색어는 존재할 수 없는 상품명으로 "결과 없음"과 동일 취급
        if (prefix.length() > 100) {
            return List.of();
        }

        try {
            //prefix로 시작하는 모든 문자열의 상한선
            //사전순으로 정렬된 집합에서 prefix로 시작하는 구간만 잘라 조회
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
        } catch (DataAccessException e) {
            //Redis 장애가 검색창을 죽이면 안 되므로 빈 결과로 대체
            log.warn("자동완성 조회 실패 — Redis 연결 확인 필요", e);
            return List.of();
        }
    }

    //검색 실행 시 호출 — 해당 키워드 점수 +1
    public void recordKeyword(String keyword){String trimmed = keyword.trim();
        //비었거나 비정상적으로 긴 검색어는 집계 제외
        if (trimmed.isEmpty() || trimmed.length() > 30) {
            return;
        }
        try {
            redisTemplate.opsForZSet().incrementScore(POPULAR_KEY, trimmed, 1);
        } catch (DataAccessException e) {
            log.warn("인기 검색어 기록 실패 Redis 연결 확인 필요", e);
        }
    }

    //인기 검색어 톱 8
    public List<String> getPopularKeywords(){
        try {
            Set<String> top = redisTemplate.opsForZSet().reverseRange(POPULAR_KEY, 0, 7);
            return top == null ? List.of() : List.copyOf(top);
        } catch (DataAccessException e) {
            return List.of();   //Redis 장애 시 빈 목록
        }
    }
}
