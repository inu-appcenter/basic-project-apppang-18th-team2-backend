package com.apppang.apppang2.domain.address.entity;

import com.apppang.apppang2.domain.address.dto.request.AddressUpdateRequest;
import com.apppang.apppang2.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="addresses")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)   //빌더를 통해서만 객체를 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  //유저정보가 필요한 순간에만 유저 테이블을 가져온다
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private String receiverPhone;

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = false)
    private String detailAddress;

    private boolean isDefault;          //기본 배송지 여부

    public void updateDefault(boolean isDefault){
        this.isDefault = isDefault;
    }

    //배송지 정보 수정 메서드
    public void updateAddress(AddressUpdateRequest updateRequest){
        this.receiver = updateRequest.getReceiver();
        this.receiverPhone = updateRequest.getReceiverPhone();
        this.roadAddress = updateRequest.getRoadAddress();
        this.detailAddress = updateRequest.getDetailAddress();
    }
}
