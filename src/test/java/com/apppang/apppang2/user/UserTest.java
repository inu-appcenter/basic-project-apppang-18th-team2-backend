package com.apppang.apppang2.user;


import com.apppang.apppang2.domain.user.entity.Role;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;



import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest     //스프링 부트의 모든 빈을 로드
@AutoConfigureMockMvc   //MockMvc를 사용하기 위해 필요
@Transactional      //테스트가 끝나면 DB를 롤백하여 데이터 남지 않도록 함
class UserTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private String uniqueEmail;

    @BeforeEach
    void setUp() {
        //테스트 실행 전 기본 테스트 유저를 미리 DB에 저장
        uniqueEmail = "test_"+System.currentTimeMillis()+"@test.com";
        User user = User.builder()
                .email(uniqueEmail)
                .name("홍길동")
                .password("password123!")
                .phone("01012345678")
                .role(Role.USER)
                .agreeRequiredTerms(true)
                .agreeMarketing(false)
                .deleted(false)
                .build();

        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_success() throws Exception{
        String userIdStr =  String.valueOf(savedUser.getId());

        // when & then : 인증된 사용자ID로 내 정보 조회 API 호출
        mockMvc.perform(get("/api/users/me")
                        .with(user(userIdStr)))     //요청 보내기
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(uniqueEmail)) //이메일 값 검증
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andDo(print());
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 존재하지 않는 회원ID로 조회")
    void getMyInfo_notFound_fail() throws Exception{
        String notExistUserIdStr = "99999";

        // when & then : DB에 존재하지 않는 회원ID로 조회를 시도하면 에러 반환검증
        mockMvc.perform(get("/api/users/me")
                        .with(user(notExistUserIdStr)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateMyInfo_success() throws Exception{
        String userIdStr = String.valueOf(savedUser.getId());

        //서버로 보낼 수정 요청 데이터
        String requestBody = "{\"name\":\"생수\",\"phone\":\"01012345678\"}";

        //when: 정보 수정 PATCH 요청 전송
        mockMvc.perform(patch("/api/users/me")
                        .with(user(userIdStr))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(print());

        //then: DB에서 유저를 다시 조회해 실제 값이 변경되었는지 확인
        User updateUser = userRepository.findById(savedUser.getId()).get();
        assertThat(updateUser.getName()).isEqualTo("생수");
        assertThat(updateUser.getPhone()).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 올바르지 않는 형식의 데이터로 수정 요청")
    void updateMyInfo_invalid_fail() throws Exception{
        String userIdStr = String.valueOf(savedUser.getId());
        //이름이나 전화번호가 비어있는 경우
        String requestBody = "{\"name\":\"\",\"phone\":\"01013345678\"}";

        // when & then: 존재하지 않는 유저가 수정을 시도할 때 404 에러 반환검증
        mockMvc.perform(patch("/api/users/me")
                        .with(user(userIdStr))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 회원이 수정을 요청")
    void updateMyInfo_notFound_fail() throws Exception{
        String notExistUserIdStr = "99999";
        String requestBody = "{\"name\":\"홍동\",\"phone\":\"01013345678\"}";

        // when & then: 존재하지 않는 유저가 수정을 시도할 때 404 에러 반환검증
        mockMvc.perform(patch("/api/users/me")
                        .with(user(notExistUserIdStr))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteMyInfo_success() throws Exception {
        String userIdStr = String.valueOf(savedUser.getId());

        // when: 회원 탈퇴 DELETE 요청 전송
        mockMvc.perform(delete("/api/users/me")
                        .with(user(userIdStr)))
                .andExpect(status().isOk())
                .andDo(print());

        //then: 탈퇴 후 유저 상태값이 변경되었는지 DB에서 직접 확인
        User deletedUser = userRepository.findById(savedUser.getId()).get();
        assertThat(deletedUser.isDeleted()).isTrue();
        assertThat(deletedUser.getName()).isEqualTo("탈퇴한 사용자");

    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원이 탈퇴 요청")
    void deleteMyInfo_notFound_fail() throws Exception {
        String notExistUserIdStr = "99999";

        // when & then: 존재하지 않는 유저의 탈퇴 요청 시 404 에러 반환검증
        mockMvc.perform(delete("/api/users/me")
                        .with(user(notExistUserIdStr)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."))
                .andDo(print());
    }
}
