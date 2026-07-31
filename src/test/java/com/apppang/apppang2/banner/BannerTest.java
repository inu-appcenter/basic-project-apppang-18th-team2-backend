package com.apppang.apppang2.banner;

import com.apppang.apppang2.domain.banner.entity.Banner;
import com.apppang.apppang2.domain.banner.repository.BannerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BannerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BannerRepository bannerRepository;

    @BeforeEach
    void setUp(){
        //각 테스트가 실행되기 전에 배너 테이블의 데이터를 모두 비워 초기 상태 보장
        bannerRepository.deleteAll();
    }

    @Test
    @DisplayName("메인 배너 조회 - 할성화된 배너만 표시 순서대로 조회 성공")
    void getBanner_success() throws Exception{
        //given
        Banner banner1 = Banner.builder()
                .title("두번째로 보일 배너")
                .imageUrl("https://example.com/img2.png")
                .targetUrl("https://example.com/target2")
                .displayOrder(2)
                .active(true)
                .build();

        Banner banner2 = Banner.builder()
                .title("첫번째로 보일 배너")
                .imageUrl("https://example.com/img1.png")
                .targetUrl("https://example.com/target1")
                .displayOrder(1)
                .active(true)
                .build();

        Banner inactiveBanner = Banner.builder()
                .title("비활성화된 배너")
                .imageUrl("https://example.com/img3.png")
                .targetUrl("https://example.com/target3")
                .displayOrder(0)
                .active(false)
                .build();

        bannerRepository.saveAll(List.of(banner1, banner2, inactiveBanner));

        //when&then
        mockMvc.perform(get("/api/banners")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("메인 배너 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.length()").value(2)) // 비활성화된 배너는 제외되어야 함
                .andExpect(jsonPath("$.data[0].title").value("첫번째로 보일 배너"))
                .andExpect(jsonPath("$.data[1].title").value("두번째로 보일 배너"))
                .andDo(print());
    }

    @Test
    @DisplayName("메인 배너 조회 실패/예외 케이스 - 할성화된 배너가 없을 경우 빈 목록 반환")
    void getBanner_empty_fail() throws Exception{
        //given: 활성화된 배너는 없고 비활성화된 배너만 존재할 때
        Banner inactiveBanner = Banner.builder()
                .title("비활성화된 배너")
                .imageUrl("https://example.com/img3.png")
                .targetUrl("https://example.com/target3")
                .displayOrder(1)
                .active(false)
                .build();

        bannerRepository.save(inactiveBanner);

        //when&then
        mockMvc.perform(get("/api/banners")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("메인 배너 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())    //데이터가 배열타입인지 확인
                .andExpect(jsonPath("$.data.length()").value(0))  //데이터가 빈 배열로 오는지 검증
                .andDo(print());
    }
}
