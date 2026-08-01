package com.apppang.apppang2;

import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//헬퍼
public abstract class ScenarioTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    protected static final long NON_EXISTENT_PRODUCT_ID = 999_999_999L;
    protected static final long NON_EXISTENT_ADDRESS_ID = 999_999_999L;
    protected static final long NON_EXISTENT_ORDER_ID = 999_999_999L;

    protected final String password = "Test1234!";

    private final List<Runnable> cleanupTasks = new ArrayList<>();

    /**
     * 테스트 중 생성한 데이터를 나중에 지우도록 등록.
     * 예: 주문/배송지 도메인 테스트에서도 데이터를 만들면 이 메서드로 등록하면 됨.
     */
    protected void registerCleanup(Runnable task) {
        cleanupTasks.add(task);
    }

    @AfterAll
    void cleanUpAll() {
        List<Runnable> reversed = new ArrayList<>(cleanupTasks);
        Collections.reverse(reversed);
        for (Runnable task : reversed) {
            try {
                task.run();
            } catch (Exception e) {
                // 정리 중 하나가 실패해도 나머지는 계속 정리 시도 (테스트 실패로는 취급하지 않음)
                System.err.println("[ScenarioTestSupport] cleanup 실패: " + e.getMessage());
            }
        }
        cleanupTasks.clear();
    }

    // 고유 이메일 생성 (UUID 기반 -> 같은 밀리초에 여러 테스트가 돌아도 충돌 없음)
    protected String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@test.com";
    }

    // 회원가입 요청 (응답 값까지 검증 + 생성된 유저를 정리 대상으로 자동 등록)
    // 반환값: 응답에 포함된 userId (실제 응답 구조: {"data": {"userId": ...}})
    protected Long signup(String email) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("name", "테스트유저");
        body.put("phone", "010-0000-0000");
        body.put("agreeRequiredTerms", true);
        body.put("agreeMarketing", false);

        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andReturn();

        registerCleanup(() -> userRepository.findByEmail(email)
                .ifPresent(userRepository::delete));

        return extractData(result).path("userId").asLong();
    }

    // 로그인 요청 후 accessToken 반환 (토큰이 비어있으면 여기서 바로 실패 원인 노출)
    protected String login(String email) throws Exception {
        Map<String, Object> body = Map.of("email", email, "password", password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = root.path("data").path("accessToken").asText(null);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "로그인 응답에 accessToken이 없습니다: " + result.getResponse().getContentAsString());
        }
        return token;
    }

    // 방금 가입한 이메일로 유저 ID 조회 (UserRepository.findByEmail 존재 가정)
    protected Long extractUserIdFromEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("가입한 유저를 찾을 수 없습니다: " + email));
    }

    // 공통 응답 포맷(ApiResponse)에서 data 필드만 추출
    protected JsonNode extractData(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data");
    }
}