package com.apppang.apppang2.domain.user.service;

import com.apppang.apppang2.domain.user.dto.request.UpdateMyInfoRequest;
import com.apppang.apppang2.domain.user.dto.response.MyInfoResponse;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public MyInfoResponse getMyInfo(Long userId) {

        // 회원 조회
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 회원입니다."));

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

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 회원입니다."));

        user.updateMyInfo(
                request.getName(),
                request.getPhone()
        );
    }

    @Transactional
    public void deleteMyInfo(Long userId) {

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 회원입니다."));

        user.delete();
    }
}
