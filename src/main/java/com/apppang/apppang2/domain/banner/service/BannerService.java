package com.apppang.apppang2.domain.banner.service;

import com.apppang.apppang2.domain.banner.dto.response.BannerResponse;
import com.apppang.apppang2.domain.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor    //생성자 자동 생성
public class BannerService {
    private final BannerRepository bannerRepository;

    public List<BannerResponse> getActiveBanners(){
        return bannerRepository.findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(BannerResponse::new)
                .toList();  //변환된 BannerReponse들을 새로운 List에 담기
    }

}
