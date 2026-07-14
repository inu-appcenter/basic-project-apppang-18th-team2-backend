package com.apppang.appgang2.domain.banner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner,Long> {

    //활성화된 배너만 표시 순서대로 정렬해서 가져오기
    //여러 개의 배너를 가져와서 List<T> 형태 사용
    List<Banner> findAllByIsActiveTrueOrderByDisplayOrderAsc();

}
