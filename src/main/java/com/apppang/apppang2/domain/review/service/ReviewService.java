package com.apppang.apppang2.domain.review.service;

import com.apppang.apppang2.domain.order.entity.OrderDetail;
import com.apppang.apppang2.domain.order.repository.OrderDetailRepository;
import com.apppang.apppang2.domain.product.repository.ProductRepository;
import com.apppang.apppang2.domain.review.dto.request.ReviewCreateRequest;
import com.apppang.apppang2.domain.review.dto.request.ReviewUpdateRequest;
import com.apppang.apppang2.domain.review.dto.response.ReviewCreateResponse;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    //리뷰 작성
    @Transactional
    public Long createReview(Long userId, ReviewCreateRequest request){
        //주문 내역 존재 검증
        OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndProductId(request.getOrderId(), request.getProductId())
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,"주문내역을 찾을 수 없습니다."));

        //해당 주문상세내역에 대한 리뷰가 존재하는지 중복검사
        if(reviewRepository.existsByOrderDetailId(orderDetail.getId())){
            throw new CustomException(HttpStatus.BAD_REQUEST,"이미 리뷰를 작성한 주문입니다.");
        }

        //유저 존재 검증
        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));


        //본인의 주문 내역인지 검증(OrderDetail->Order->userId가 리뷰작성의 userId와 맞는지 확인)
        if(!orderDetail.getOrder().getUserId().equals(userId)){
            throw new CustomException(HttpStatus.CONFLICT,"본인의 주문 내역에만 작성할 수 있습니다.");
        }

        //결제완료된 상태에서만
        //if(orderDetail.getOrder().getOrderStatus()!= OrderStatus.DELIVERED || orderDetail.getOrder().getOrderStatus()!= OrderStatus.PAID){
        //    throw new CustomException(HttpStatus.FORBIDDEN,"구매한 상품만 리뷰를 작성할 수 있습니다.");
        //}

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
            url2 = imageUrls.size() > 1 ? imageUrls.get(1) : null;
        }

        Review review = Review.builder()
                .user(user)
                .productId(orderDetail.getProduct().getId())
                .orderDetail(orderDetail)
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
        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        if(!productRepository.existsById(productId)){
            throw new CustomException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }

        //DB에서 해당 상품의 리뷰 목록을 Slice 방식으로 가져오기
        Slice<Review> reviews = reviewRepository.findReviewsWithUserByProductId(productId, pageable);

        List<Long> reviewIds = reviews.getContent().stream()
                .map(Review::getId)
                .toList();


        //로그인 유저의 도움돼요를 누른 리뷰ID 목록 조회
        Set<Long> likedReviewIds = getLikedReviewIds(userId, reviewIds);

        List<ReviewDetailResponse> reviewDetails = reviews.getContent().stream()
                .map(like -> ReviewDetailResponse.of(like, user, likedReviewIds.contains(like.getId())))
                        .toList();  //리스트 형태로 반환

        //최종적으로 페이징 정보와 함께 묶어서 반환
        return ReviewListResponse.builder()
                .reviews(reviewDetails)
                .page(pageable.getPageNumber())
                .hasNext(reviews.hasNext())
                .build();
    }

    //현재 화면의 리뷰들 중 로그인한 유저가 도움돼요를 누른 리뷰 ID만 Set으로 반환
    private Set<Long> getLikedReviewIds(Long userId, List<Long> reviewIds){
        //조회된 리뷰가 아예 없으면 빈 Set 반환
        if(reviewIds.isEmpty()){
            return Collections.emptySet();
        }

        //IN 절로 한번에 조회 후 Set으로 반환
        return reviewLikeRepository.findByUserIdAndReviewIdIn(userId, reviewIds).stream()
                .map(ReviewLike::getReviewId)
                .collect(Collectors.toSet());
    }

    //리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, Long userId, ReviewUpdateRequest request){
        //리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        //리뷰 작성자와 로그인한 유저가 같은지 확인
        if(!review.getUser().getId().equals(userId)){
            throw new CustomException(HttpStatus.FORBIDDEN, "본인이 작성한 리뷰만 수정할 수 있습니다.");
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

        //연관된 도움돼요 삭제
        reviewLikeRepository.deleteByReviewId(reviewId);

        //TODO:S3에 저장된 실제 이미지 파일 삭제

        //DB에서 완전 삭제
        reviewRepository.delete(review);
    }

    //도움돼요
    @Transactional
    public ReviewLikeResponse helpedReview(Long reviewId, Long userId){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        //특정 유저의 해당 리뷰 '도움돼요' 누름 상태 확인
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);

        boolean liked;

        if(existingLike.isPresent()){
            reviewLikeRepository.delete(existingLike.get());
            review.decreaseHelpCount();
            liked = false;
        }else{
            ReviewLike newLike = ReviewLike.builder()
                    .reviewId(reviewId)
                    .userId(userId)
                    .build();

            reviewLikeRepository.save(newLike);
            review.increaseHelpCount();
            liked = true;
        }

        return ReviewLikeResponse.builder()
                .liked(liked)
                .helpCount(review.getHelpCount())
                .build();
    }
}
