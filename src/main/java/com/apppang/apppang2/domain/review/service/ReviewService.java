package com.apppang.apppang2.domain.review.service;

import com.apppang.apppang2.domain.review.dto.request.ReviewCreateRequest;
import com.apppang.apppang2.domain.review.dto.request.ReviewUpdateRequest;
import com.apppang.apppang2.domain.review.dto.response.ReviewDetailResponse;
import com.apppang.apppang2.domain.review.dto.response.ReviewLikeResponse;
import com.apppang.apppang2.domain.review.dto.response.ReviewListResponse;
import com.apppang.apppang2.domain.review.entity.Review;
import com.apppang.apppang2.domain.review.entity.ReviewLike;
import com.apppang.apppang2.domain.review.repository.ReviewLikeRepository;
import com.apppang.apppang2.domain.review.repository.ReviewRepository;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.apppang.apppang2.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    //리뷰 작성
    @Transactional
    public Long createReview(Long userId, ReviewCreateRequest request){

        //해당 주문상세내역에 대한 리뷰가 존재하는지 중복검사
        if(reviewRepository.existsByOrderDetailId(request.getOrderDetailId())){
            throw new CustomException(HttpStatus.BAD_REQUEST,"이미 리뷰를 작성한 주문입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        /*TODO : 주문상세내역조회하여 조회할 수 없으면 에러
        new CustomException(HttpStatus.CONFLICT,"구매한 상품만 리뷰를 작성할 수 있습니다.");

        TODO : 본인의 주문내역이 맞는지 검증
        new CustomException(HttpStatus.FORBIDDEN,"구매한 상품만 리뷰를 작성할 수 있습니다.");
        */


        //사진이 한장도 오지 않으면 null 상태로 저장
        String url1 = null;
        String url2 = null;
        List<String> imageUrls = request.getImageUrls();

        if(imageUrls != null && !imageUrls.isEmpty()){
            if(imageUrls.size() > 2){
                throw new CustomException(HttpStatus.BAD_REQUEST,"이미지는 최대 2장까지 첨부할 수 있습니다.");
            }
            //리스트에서 데이터 가져오기
            url1 = imageUrls.get(0);
            if(imageUrls.size() == 2){
                url2 = imageUrls.get(1);
            }
        }

        Review review = Review.builder()
                .user(user)
                //TODO : OrderDetail 완성되면 완성
                //.product()
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl1(url1)
                .imageUrl2(url2)
                .build();

        Review savedReview = reviewRepository.save(review);
        return savedReview.getId();
    }

    //리뷰 조회
    @Transactional(readOnly = true)
    public ReviewListResponse getReviews(Long productId, Long userId, Pageable pageable) {

        //DB에서 해당 상품의 리뷰 목록을 Slice 방식으로 가져오기
        Slice<Review> reviewSlice = reviewRepository.findByProductId(productId, pageable);

        //현재 페이지에 조회된 리뷰들의 ID만 추출하여 리스트로 만들기
        List<Long> reviewIds = reviewSlice.getContent().stream()
                .map(Review::getId)
                .toList();

        //현재 화면을 보는 userId가 이 리뷰들 중 도움돼요를 누른 기록을 한번에 조회
        Set<Long> likedReviewIds = Collections.emptySet();
        if(userId != null && !reviewIds.isEmpty()){
            likedReviewIds = reviewLikeRepository.findByReviewIdAndUserId(userId, reviewIds).stream()
                    .map(like->like.getReview().getId())
                    .collect(Collectors.toSet());
        }

        Set<Long> finalLikedReviewIds = likedReviewIds;

        List<ReviewDetailResponse> reviewDetails = reviewSlice.getContent().stream()
                .map(review -> {
                    //이름 마스킹(홍*동)
                    String name = review.getUser().getName();
                    String maskedName = name.charAt(0) + "*" + (name.length() > 2 ? name.substring(2) : "");

                    //이미지url이 null이 아니라면 이미지 리스트에 추가
                    List<String> images = new ArrayList<>();
                    if(review.getImageUrl1()!=null){
                        images.add(review.getImageUrl1());
                    }
                    if(review.getImageUrl2()!=null){
                        images.add(review.getImageUrl2());
                    }

                    //날짜 yyyy-MM-dd로 포맷
                    String createdAt = review.getCreatedAt() != null ? review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")):"";

                    //Set을 이용해 현재 리뷰에 좋아요를 눌렀는지 확인
                    boolean isHelped = finalLikedReviewIds.contains(review.getId());

                    return ReviewDetailResponse.builder()
                            .reviewId(review.getId())
                            .userName(maskedName)
                            .rating(review.getRating())
                            .content(review.getContent())
                            .images(images)
                            .createdAt(createdAt)
                            .helpCount(review.getHelpCount())
                            .helped(isHelped)
                            .build();
                })
                .toList();  //리스트 형태로 반환

        //최종적으로 페이징 정보와 함께 묶어서 반환
        return new ReviewListResponse(reviewDetails, pageable.getPageNumber(), reviewSlice.hasNext());
    }

    //리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, Long userId, ReviewUpdateRequest request){
        //리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        //리뷰 작성자와 로그인한 유저가 같은지 확인
        if(!review.getUser().getId().equals(userId)){
            throw new CustomException(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 찾을 수 있습니다.");
        }

        //content 양 끝 공백 제거하여 엔티티에 반환
        review.update(request.getRating(), request.getContent().trim());
    }

    //리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, Long userId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if(!review.getUser().getId().equals(userId)){
            throw new CustomException(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        //DB에서 완전 삭제
        reviewRepository.delete(review);
    }

    //도움돼요
    @Transactional
    public ReviewLikeResponse helpedReview(Long reviewId, Long userId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        //특정 유저의 해당 리뷰 '도움돼요' 누름 상태 확인
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(userId, reviewId);

        boolean liked;

        if(existingLike.isPresent()){
            reviewLikeRepository.delete(existingLike.get());
            review.decreaseHelpCount();
            liked = false;
        }else{
            ReviewLike newLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();

            reviewLikeRepository.save(newLike);
            review.increaseHelpCount();
            liked = true;
        }

        return new ReviewLikeResponse(liked, review.getHelpCount());
    }
}
