package com.apppang.apppang2.domain.user.controller;

import com.apppang.apppang2.domain.user.dto.response.MyInfoResponse;
import com.apppang.apppang2.domain.user.service.UserService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
