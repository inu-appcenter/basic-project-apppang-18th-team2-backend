package com.apppang.apppang2.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyInfoResponse {

    private Long userId;

    private String email;

    private String name;

    private String phone;

    private String profileImage;
}
