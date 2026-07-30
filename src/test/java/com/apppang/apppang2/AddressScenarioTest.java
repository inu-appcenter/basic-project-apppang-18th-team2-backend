package com.apppang.apppang2;

import com.apppang.apppang2.domain.address.entity.Address;
import com.apppang.apppang2.domain.address.repository.AddressRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Address 도메인 시나리오 테스트

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddressScenarioTest extends ScenarioTestSupport {

    @Autowired
    private AddressRepository addressRepository;

    private final String mainEmail = uniqueEmail("scenario.address.main");
    private final String otherEmail = uniqueEmail("scenario.address.other");

    private String mainAccessToken;
    private String otherAccessToken;
    private Long mainUserId;
    private Long otherUserId;

    private Long addressId1;
    private Long addressId2;
    private Long addressId3;

    @Test
    @Order(1)
    @DisplayName("[준비] 메인/보조 유저 회원가입 및 로그인")
    void signup_and_login_users() throws Exception {
        signup(mainEmail);
        mainAccessToken = login(mainEmail);
        mainUserId = extractUserIdFromEmail(mainEmail);

        signup(otherEmail);
        otherAccessToken = login(otherEmail);
        otherUserId = extractUserIdFromEmail(otherEmail);

        Assertions.assertNotNull(mainAccessToken);
        Assertions.assertNotNull(otherAccessToken);

        // signup()이 이미 "유저 삭제"를 등록해뒀으므로, 그보다 나중에 등록해서
        // LIFO 순서상 주소가 유저보다 먼저 삭제되도록 함 (FK 안전)
        registerCleanup(() -> addressRepository.findByUserId(mainUserId).forEach(addressRepository::delete));
        registerCleanup(() -> addressRepository.findByUserId(otherUserId).forEach(addressRepository::delete));
    }

    @Test
    @Order(10)
    @DisplayName("[로그인] 배송지가 없는 상태에서 배송지 수정 요청 -> 404")
    void address_update_whenNoAddressExists() throws Exception {
        Map<String, Object> body = Map.of(
                "receiver", "홍길동",
                "receiverPhone", "010-1234-5678",
                "roadAddress", "서울시 강남구 테헤란로",
                "detailAddress", "1층"
        );

        mockMvc.perform(patch("/api/addresses/{addressId}", NON_EXISTENT_ADDRESS_ID)
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    @DisplayName("[로그인] 첫 배송지 추가 (기본배송지로 명시)")
    void address_add_first() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("receiver", "김철수");
        body.put("receiverPhone", "010-1111-1111");
        body.put("roadAddress", "서울시 마포구 월드컵로");
        body.put("detailAddress", "101동 101호");
        body.put("isDefault", true);

        MvcResult result = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        addressId1 = extractData(result).asLong();

        Address saved = addressRepository.findById(addressId1).orElseThrow();
        Assertions.assertTrue(saved.isDefault());
    }

    @Test
    @Order(12)
    @DisplayName("[로그인] 배송지 목록 조회 (1개)")
    void address_getList_afterFirst() throws Exception {
        mockMvc.perform(get("/api/addresses")
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @Order(13)
    @DisplayName("[로그인] 두 번째 배송지 추가 (기본 아님)")
    void address_add_second_notDefault() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("receiver", "이영희");
        body.put("receiverPhone", "010-2222-2222");
        body.put("roadAddress", "서울시 서초구 서초대로");
        body.put("detailAddress", "202동 202호");
        body.put("isDefault", false);

        MvcResult result = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        addressId2 = extractData(result).asLong();

        Address saved = addressRepository.findById(addressId2).orElseThrow();
        Assertions.assertFalse(saved.isDefault());
    }

    @Test
    @Order(14)
    @DisplayName("[로그인] 배송지 목록 조회 (2개)")
    void address_getList_afterSecond() throws Exception {
        mockMvc.perform(get("/api/addresses")
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @Order(15)
    @DisplayName("[로그인] 세 번째 배송지 추가 (기본배송지로 명시) - 기존 기본배송지 해제 확인")
    void address_add_third_asDefault() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("receiver", "박민수");
        body.put("receiverPhone", "010-3333-3333");
        body.put("roadAddress", "서울시 송파구 올림픽로");
        body.put("detailAddress", "303동 303호");
        body.put("isDefault", true);

        MvcResult result = mockMvc.perform(post("/api/addresses")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        addressId3 = extractData(result).asLong();

        long defaultCount = addressRepository.findByUserId(mainUserId).stream()
                .filter(Address::isDefault)
                .count();
        Assertions.assertEquals(1, defaultCount, "기본배송지는 항상 1개여야 합니다.");
    }

    @Test
    @Order(16)
    @DisplayName("[로그인] 기본 배송지를 다른 배송지로 변경")
    void address_changeDefault() throws Exception {
        mockMvc.perform(patch("/api/addresses/{addressId}/default", addressId2)
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDefault").value(true));

        long defaultCount = addressRepository.findByUserId(mainUserId).stream()
                .filter(Address::isDefault)
                .count();
        Assertions.assertEquals(1, defaultCount);
    }

    @Test
    @Order(17)
    @DisplayName("[로그인] 기본 배송지 삭제 시도 -> 409")
    void address_delete_defaultAddress_conflict() throws Exception {
        mockMvc.perform(delete("/api/addresses/{addressId}", addressId2)
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(18)
    @DisplayName("[로그인] 기본배송지가 아닌 배송지 삭제 -> 성공")
    void address_delete_nonDefaultAddress_success() throws Exception {
        mockMvc.perform(delete("/api/addresses/{addressId}", addressId1)
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Assertions.assertTrue(addressRepository.findById(addressId1).isEmpty());
    }

    @Test
    @Order(19)
    @DisplayName("[로그인] 존재하지 않는 배송지에 대한 수정/기본지정/삭제 -> 모두 404")
    void address_notFound_operations() throws Exception {
        Map<String, Object> updateBody = Map.of(
                "receiver", "홍길동",
                "receiverPhone", "010-0000-0000",
                "roadAddress", "존재하지않는주소",
                "detailAddress", "0층"
        );

        mockMvc.perform(patch("/api/addresses/{addressId}", NON_EXISTENT_ADDRESS_ID)
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/addresses/{addressId}/default", NON_EXISTENT_ADDRESS_ID)
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/addresses/{addressId}", NON_EXISTENT_ADDRESS_ID)
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(20)
    @DisplayName("[로그인] 타인의 배송지 수정/기본지정/삭제 시도 -> 모두 403")
    void address_otherUser_forbidden() throws Exception {
        Map<String, Object> updateBody = Map.of(
                "receiver", "침입시도",
                "receiverPhone", "010-9999-9999",
                "roadAddress", "남의 주소",
                "detailAddress", "몰래"
        );

        mockMvc.perform(patch("/api/addresses/{addressId}", addressId3)
                        .header("Authorization", "Bearer " + otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/addresses/{addressId}/default", addressId3)
                        .header("Authorization", "Bearer " + otherAccessToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/addresses/{addressId}", addressId3)
                        .header("Authorization", "Bearer " + otherAccessToken))
                .andExpect(status().isForbidden());
    }
}