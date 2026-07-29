package com.apppang.apppang2.domain.image.controller;

import com.apppang.apppang2.domain.image.service.ImageService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "IMAGE")
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    //파일 1장 업로드 → URL 반환 (2장이면 프론트에서 두 번 호출)
    @Operation(summary = "이미지 업로드")
    @PostMapping(value = "/api/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)     //JSON 대신 파일전송형식 사용
    public ApiResponse<Map<String, String>> upload(@AuthenticationPrincipal Long userId,
                                                   @RequestParam MultipartFile image){
        String url = imageService.upload(userId, image);
        return ApiResponse.success("이미지 업로드에 성공했습니다.", Map.of("imageUrl", url));
    }
}