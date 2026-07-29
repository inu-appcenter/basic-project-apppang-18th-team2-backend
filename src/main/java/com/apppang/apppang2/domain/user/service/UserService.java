package com.apppang.apppang2.domain.user.service;

import com.apppang.apppang2.domain.user.dto.request.UpdateMyInfoRequest;
import com.apppang.apppang2.domain.user.dto.response.MyInfoResponse;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public MyInfoResponse getMyInfo(Long userId) {

        // 회원 조회
        User user = findActiveUser(userId);

        // DTO 변환
        return MyInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .build();
    }

    @Transactional
    public void updateMyInfo(Long userId,
                             UpdateMyInfoRequest request) {

        User user = findActiveUser(userId);

        user.updateMyInfo(
                request.getName(),
                request.getPhone()
        );
    }

    @Transactional
    public void deleteMyInfo(Long userId) {

        User user = findActiveUser(userId);

        user.delete();
    }

    //중복되는 회원 조회 로직 공통화
    private User findActiveUser(Long userId){
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));
    }
}
