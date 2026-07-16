package com.apppang.apppang2.domain.user.controller;

import com.apppang.apppang2.domain.user.dto.request.UpdateMyInfoRequest;
import com.apppang.apppang2.domain.user.dto.response.MyInfoResponse;
import com.apppang.apppang2.domain.user.service.UserService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Tag(name = "USER")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<MyInfoResponse> getMyInfo(Authentication authentication){

        // JWT 적용 후 로그인한 사용자의 ID를 가져오도록 변경
        Long userId = Long.parseLong(authentication.getName());

        MyInfoResponse response = userService.getMyInfo(userId);

        return ApiResponse.success(
                "회원 정보를 조회했습니다.",
                response
        );

    }

    @Operation(summary = "회원정보 수정")
    @PatchMapping("/me")
    public ApiResponse<Void> updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody UpdateMyInfoRequest request) {

        Long userId = Long.parseLong(authentication.getName());

        userService.updateMyInfo(userId, request);

        return ApiResponse.success(
                "회원 정보가 수정되었습니다.",
                null
        );
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMyInfo(Authentication authentication){

        Long userId = Long.parseLong(authentication.getName());

        userService.deleteMyInfo(userId);

        return ApiResponse.success(
                "회원 탈퇴가 완료되었습니다."
        );
    }
}
