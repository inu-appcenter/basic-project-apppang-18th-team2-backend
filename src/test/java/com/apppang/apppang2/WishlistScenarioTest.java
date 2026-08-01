package com.apppang.apppang2;

import com.fasterxml.jackson.databind.JsonNode;
import com.apppang.apppang2.domain.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Wishlist 도메인 시나리오 테스트

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WishlistScenarioTest extends ScenarioTestSupport {

    @Autowired
    private WishlistRepository wishlistRepository;

    private static final long PRODUCT_ID_1 = 1L;
    private static final long PRODUCT_ID_2 = 2L;
    private static final long PRODUCT_ID_3 = 3L;
    private static final long PRODUCT_ID_4 = 4L;
    private static final long PRODUCT_ID_5 = 5L;

    private final String mainEmail = uniqueEmail("scenario.wishlist.main");
    private String mainAccessToken;
    private Long mainUserId;

    // 격리(isolation) 검증용 - 다른 유저의 위시리스트에 영향을 주거나 조회되지 않아야 함
    private final String otherEmail = uniqueEmail("scenario.wishlist.other");
    private String otherAccessToken;

    @Test
    @Order(1)
    @DisplayName("[비로그인] 찜 추가 시도 -> 401")
    void wishlist_add_withoutLogin_unauthorized() throws Exception {
        Map<String, Object> body = Map.of("productId", PRODUCT_ID_1);

        mockMvc.perform(post("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @DisplayName("[비로그인] 찜 목록 조회 시도 -> 401")
    void wishlist_getList_withoutLogin_unauthorized() throws Exception {
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("[비로그인] 찜 취소 시도 -> 401")
    void wishlist_delete_withoutLogin_unauthorized() throws Exception {
        Map<String, Object> body = Map.of("productId", PRODUCT_ID_1);

        mockMvc.perform(delete("/api/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("[준비] 메인 유저 / 다른 유저 회원가입 및 로그인")
    void signup_and_login() throws Exception {
        signup(mainEmail);
        mainAccessToken = login(mainEmail);
        mainUserId = extractUserIdFromEmail(mainEmail);
        Assertions.assertNotNull(mainAccessToken);
        Assertions.assertNotNull(mainUserId);

        registerCleanup(() ->
                wishlistRepository.findAllByUserIdWithProduct(mainUserId).forEach(wishlistRepository::delete));

        signup(otherEmail);
        otherAccessToken = login(otherEmail);
        Long otherUserId = extractUserIdFromEmail(otherEmail);
        registerCleanup(() ->
                wishlistRepository.findAllByUserIdWithProduct(otherUserId).forEach(wishlistRepository::delete));
    }

    @Test
    @Order(10)
    @DisplayName("[로그인] 찜 추가")
    void wishlist_add_returnsCreatedResource() throws Exception {
        Map<String, Object> body = Map.of("productId", PRODUCT_ID_1);

        mockMvc.perform(post("/api/wishlist")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(11)
    @DisplayName("[로그인] 같은 상품 중복 찜 추가 -> 409")
    void wishlist_add_duplicate() throws Exception {
        Map<String, Object> body = Map.of("productId", PRODUCT_ID_1);

        mockMvc.perform(post("/api/wishlist")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(12)
    @DisplayName("[로그인] 찜 취소")
    void wishlist_delete() throws Exception {
        Map<String, Object> body = Map.of("productId", PRODUCT_ID_1);

        mockMvc.perform(delete("/api/wishlist")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(13)
    @DisplayName("[로그인] 이미 취소된(찜 안 한) 상품 다시 취소 시도 -> 404")
    void wishlist_delete_notWished_notFound() throws Exception {
        Map<String, Object> body = Map.of("productId", PRODUCT_ID_1);

        mockMvc.perform(delete("/api/wishlist")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(14)
    @DisplayName("[로그인] 존재하지 않는 상품 찜 시도 -> 404")
    void wishlist_add_nonExistentProduct_notFound() throws Exception {
        Map<String, Object> body = Map.of("productId", NON_EXISTENT_PRODUCT_ID);

        mockMvc.perform(post("/api/wishlist")
                        .header("Authorization", "Bearer " + mainAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(15)
    @DisplayName("[로그인] 상품 1~5번 모두 찜 추가")
    void wishlist_add_multipleProducts() throws Exception {
        for (long productId : List.of(PRODUCT_ID_1, PRODUCT_ID_2, PRODUCT_ID_3, PRODUCT_ID_4, PRODUCT_ID_5)) {
            Map<String, Object> body = Map.of("productId", productId);
            mockMvc.perform(post("/api/wishlist")
                            .header("Authorization", "Bearer " + mainAccessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @Order(16)
    @DisplayName("[로그인] 찜 목록 조회 - 개수뿐 아니라 실제 productId 집합까지 정확히 일치하는지 확인")
    void wishlist_getList() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode products = extractData(result).path("products");
        assertThat(products.size()).isEqualTo(5);

        Set<Long> actualIds = new HashSet<>();
        for (JsonNode product : products) {
            actualIds.add(product.path("productId").asLong());
        }
        Set<Long> expectedIds = Set.of(PRODUCT_ID_1, PRODUCT_ID_2, PRODUCT_ID_3, PRODUCT_ID_4, PRODUCT_ID_5);
        // 개수만 5개가 아니라, "정확히 이 5개 상품"인지까지 확인 (중복/누락을 개수만으로는 못 잡음)
        assertThat(actualIds).isEqualTo(expectedIds);
    }

    @Test
    @Order(17)
    @DisplayName("[격리] 다른 유저의 찜 목록에는 내 위시리스트가 보이지 않음")
    void wishlist_isolatedBetweenUsers() throws Exception {
        // other 유저는 아무것도 찜한 적 없으므로 빈 목록이어야 함
        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Bearer " + otherAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products.length()").value(0));
    }

    @Test
    @Order(18)
    @DisplayName("[격리] 다른 유저가 내 찜 항목을 취소할 수 없음 (본인이 찜한 적 없으므로 404)")
    void wishlist_cannotDeleteOthersWish() throws Exception {
        Map<String, Object> body = Map.of("productId", PRODUCT_ID_2); // main 유저가 찜한 상품

        mockMvc.perform(delete("/api/wishlist")
                        .header("Authorization", "Bearer " + otherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        // main 유저 입장에서는 여전히 찜 상태가 유지되어 있어야 함 (다른 유저의 삭제 시도로 영향받지 않음)
        MvcResult result = mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Bearer " + mainAccessToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode products = extractData(result).path("products");
        assertThat(products.size()).isEqualTo(5);
    }
}