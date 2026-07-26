package com.apppang.apppang2.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

//한 주문에 포함된 모든 상품은 배송 정보를 공유함
@Entity
@Table(name = "delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //외부에서 실수로 빈 객체를 못만들게 제한
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    //지금은 주문 개체별로 배송 옵션을 나누는 기능이 없어 order_id를 사용함
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;


    @Column(unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(nullable = false)
    private String courier;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Builder
    public Delivery(Order order, String trackingNumber, DeliveryStatus deliveryStatus,
                    String courier, LocalDateTime startedAt, LocalDateTime completedAt){
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.deliveryStatus = deliveryStatus;
        this.courier = courier;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
}
