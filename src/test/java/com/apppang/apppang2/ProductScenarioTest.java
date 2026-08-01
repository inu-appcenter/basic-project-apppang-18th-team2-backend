package com.apppang.apppang2;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Product 도메인 시나리오 테스트

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductScenarioTest extends ScenarioTestSupport {

    private static final long PRODUCT_ID_1 = 1L; // fixture에 존재하는 상품 ID 가정

    @Test
    @Order(1)
    @DisplayName("상품 목록 조회 - priceAsc 정렬 시 가격 오름차순 확인")
    void product_search_sortByPriceAsc() throws Exception {
        assertSorted(getPriceList("priceAsc"), true);
    }

    @Test
    @Order(2)
    @DisplayName("상품 목록 조회 - priceDesc 정렬 시 가격 내림차순 확인")
    void product_search_sortByPriceDesc() throws Exception {
        assertSorted(getPriceList("priceDesc"), false);
    }

    @Test
    @Order(3)
    @DisplayName("상품 목록 조회 - rating 정렬 시 평점 내림차순 확인")
    void product_search_sortByRating() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("sort", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(1))
                .andReturn();

        JsonNode items = extractData(result).path("items");
        double prevRating = Double.MAX_VALUE;
        for (JsonNode item : items) {
            double rating = item.path("rating").asDouble();
            assertThat(rating).isLessThanOrEqualTo(prevRating);
            prevRating = rating;
        }
    }

    @Test
    @Order(4)
    @DisplayName("상품 목록 조회 - latest 정렬 시 최신순(createdAt 내림차순) 확인")
    void product_search_sortByLatest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andReturn();

        JsonNode items = extractData(result).path("items");
        String prevCreatedAt = null;
        for (JsonNode item : items) {
            String createdAt = item.path("createdAt").asText();
            if (prevCreatedAt != null) {
                assertThat(createdAt.compareTo(prevCreatedAt)).isLessThanOrEqualTo(0);
            }
            prevCreatedAt = createdAt;
        }
    }

    @Test
    @Order(5)
    @DisplayName("상품 목록 조회 - sort 생략 시 latest와 동일한 결과인지 확인 (기본값=최신순)")
    void product_search_defaultSort() throws Exception {
        MvcResult withoutSort = mockMvc.perform(get("/api/products").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        MvcResult withLatest = mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("sort", "latest"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode itemsWithoutSort = extractData(withoutSort).path("items");
        JsonNode itemsWithLatest = extractData(withLatest).path("items");
        // 단순히 200/success만 보는 게 아니라, "생략 = latest"라는 주장을 실제로 검증
        assertThat(itemsWithoutSort.toString()).isEqualTo(itemsWithLatest.toString());
    }

    @Test
    @Order(6)
    @DisplayName("상품 목록 조회 - 허용되지 않은 sort 값 -> 400")
    void product_search_invalidSort() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("sort", "이상한값"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("상품 목록 조회 - page=-1 -> 400")
    void product_search_negativePage() throws Exception {
        mockMvc.perform(get("/api/products").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }


    @Test
    @Order(8)
    @DisplayName("상품 상세 조회 - 존재하지 않는 상품 -> 404")
    void product_detail_notFound() throws Exception {
        mockMvc.perform(get("/api/products/{productId}", NON_EXISTENT_PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(9)
    @DisplayName("상품 상세 조회 - 존재하는 상품 -> 200, 상세 필드까지 검증")
    void product_detail_found() throws Exception {
        mockMvc.perform(get("/api/products/{productId}", PRODUCT_ID_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(PRODUCT_ID_1))
                .andExpect(jsonPath("$.data.name").isNotEmpty())
                .andExpect(jsonPath("$.data.originalPrice").isNumber())
                .andExpect(jsonPath("$.data.discountRate").exists());
    }

    // ---- 내부 헬퍼 ----

    private List<Long> getPriceList(String sort) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("sort", sort))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(1))
                .andReturn();

        JsonNode items = extractData(result).path("items");
        List<Long> prices = new ArrayList<>();
        for (JsonNode item : items) {
            prices.add(item.path("price").asLong());
        }
        return prices;
    }

    private void assertSorted(List<Long> values, boolean ascending) {
        for (int i = 1; i < values.size(); i++) {
            if (ascending) {
                assertThat(values.get(i)).isGreaterThanOrEqualTo(values.get(i - 1));
            } else {
                assertThat(values.get(i)).isLessThanOrEqualTo(values.get(i - 1));
            }
        }
    }
}