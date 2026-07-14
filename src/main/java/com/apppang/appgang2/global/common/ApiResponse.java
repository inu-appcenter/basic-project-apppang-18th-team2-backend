package com.apppang.appgang2.global.common;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)   //success()와 fail()만 사용하도록 강제
//공통 응답 규격 클래스
public class ApiResponse<T> {
    private boolean success;    //API 요청 성공/실패 여부
    private String message;
    private T data;             //<T>를 사용하여 모든 형태의 데이터를 담는 역할

    //성공 응답(데이터 있을 때)
    public static <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, message, data);
    }

    //성공 응답(데이터 없을 때)
    public static <T> ApiResponse<T> success(String message){
        return new ApiResponse<>(true, message, null);
    }

    //실패 응답(에러 발생)
    public static <T> ApiResponse<T> fail(String message){
        return new ApiResponse<>(false, message, null);
    }


}

