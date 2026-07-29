package com.apppang.apppang2.domain.banner.repository;

import com.apppang.apppang2.domain.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner,Long> {

    //활성화된 배너만 표시 순서대로 정렬해서 가져오기
    List<Banner> findAllByActiveTrueOrderByDisplayOrderAsc();

}
