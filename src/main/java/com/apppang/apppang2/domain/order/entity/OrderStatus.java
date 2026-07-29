package com.apppang.apppang2.domain.order.entity;

//주문 상태
public enum OrderStatus {
    PENDING,     //주문은 생성되었으나 결제 대기중인 상태
    PAID,        //결제 완료
    PREPARING,   //상품 준비 중
    DELIVERING,  //배송 중
    DELIVERED,   //배송 완료
    CANCELED     //취소됨
}