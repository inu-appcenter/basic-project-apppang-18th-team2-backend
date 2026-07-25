package com.apppang.apppang2.domain.search.controller;

import com.apppang.apppang2.domain.search.service.SearchService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SEARCH")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    //자동완성
    @Operation(summary = "자동완성")
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> getAutocomplete(@RequestParam String keyword){
        List<String> result = searchService.getAutoComplete(keyword);
        return ResponseEntity.ok(ApiResponse.success("자동완성 조회에 성공했습니다.",result));
    }
}
