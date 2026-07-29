package com.apppang.apppang2.domain.payment.entity;

//결제 상태  결제요청, 결제성공, 결제실패, 결제취소
public enum PaymentStatus {
    REQUESTED,   //결제요청
    SUCCESS,     //결제성공
    FAILED,      //결제실패
    CANCELED     //결제취소
}