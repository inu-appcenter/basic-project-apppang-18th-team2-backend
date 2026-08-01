package com.apppang.apppang2.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 하위 구조가 OrderDetailResponse 밖에서 쓰일 일이 없고
// 종속된 개념인걸 확실시 하고 싶어 아래 구조를 채택
@Getter
@Builder
public class OrderDetailResponse {
    private Long orderId;
    private LocalDateTime orderedAt;
    private String orderStatus;
    private PaymentInfo payment;
    private ReceiverInfo receiver;
    private AddressInfo address;
    private List<OrderItemInfo> items;
    private SummaryInfo summary;

    @Getter
    @Builder
    public static class PaymentInfo {
        private String paymentMethod;
        private String paymentStatus;
        private LocalDateTime paidAt;
    }

    @Getter
    @Builder
    public static class ReceiverInfo {
        private String name;
        private String phone;
    }

    @Getter
    @Builder
    public static class AddressInfo {
        private String roadAddress;
        private String detailAddress;
    }

    @Getter
    @Builder
    public static class OrderItemInfo {
        private Long productId;
        private String productName;
        private String thumbnail;
        private int originalPrice;
        private int discountRate;
        private int salePrice;
        private int quantity;
        private int totalPrice;

        //이 주문상세에 작성된 내 리뷰 (없으면 null — 프론트의 "리뷰 작성"↔"수정·삭제" 버튼 전환 및 수정 화면 초기값용)
        private Long reviewId;
        private Double reviewRating;
        private String reviewContent;
        private List<String> reviewImages;
    }

    @Getter
    @Builder
    public static class SummaryInfo {
        private int productPrice;
        private int deliveryFee;
        private int discountPrice;
        private int totalPrice;
    }
}