package com.apppang.apppang2.domain.banner;

import com.apppang.apppang2.domain.banner.dto.BannerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor    //생성자 자동 생성
public class BannerService {
    private final BannerRepository bannerRepository;

    public List<BannerResponse> getActiveBanners(){
        //bannerRepository에 있는 List<Banner>들이 나오고
        return bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()   //여러 개의 Banner들이 흘러 map()을 통과해 BannerResponse로 변환됨
                .map(banner -> new BannerResponse(banner))
                .toList();  //변환된 BannerReponse들을 새로운 List에 담기
    }

}
