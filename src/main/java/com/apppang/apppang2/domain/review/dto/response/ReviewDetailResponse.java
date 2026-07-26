package com.apppang.apppang2.domain.review.dto.response;

import com.apppang.apppang2.domain.review.entity.Review;
import com.apppang.apppang2.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ReviewDetailResponse {
    private final Long reviewId;
    private final String userName;
    private final Double rating;
    private final String content;
    private final List<String> images;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") //날짜 yyyy-MM-dd로 포맷
    private final LocalDateTime createdAt;


    private final int helpCount;
    private final boolean helped;      //현재 로그인한 유저가 이 리뷰에 도움돼요를 눌렀는지 여부

    public static ReviewDetailResponse of(Review review, User user, boolean isHelped){
        return ReviewDetailResponse.builder()
                .reviewId(review.getId())
                .userName(maskName(user.getName()))
                .rating(review.getRating())
                .content(review.getContent())
                .images(extractImages(review))
                .createdAt(review.getCreatedAt())
                .helpCount(review.getHelpCount())
                .helped(isHelped)
                .build();
    }
    private static String maskName(String name){
        if(name==null||name.isBlank()) {
            return name;
        }
        int length = name.length();
        if(length==1){
            return name;
        }else if(length==2){
            return name.charAt(0) + "*";
        } else {
            return name.charAt(0) + "*" + name.substring(2);
        }
    }

    private static List<String> extractImages(Review review){
        //이미지url이 null이 아니라면 이미지 리스트에 추가
        List<String> images = new ArrayList<>();
        if(review.getImageUrl1()!=null){
            images.add(review.getImageUrl1());
        }
        if(review.getImageUrl2()!=null){
            images.add(review.getImageUrl2());
        }
        return images;
    }
}
