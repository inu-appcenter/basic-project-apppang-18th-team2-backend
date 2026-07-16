package com.apppang.apppang2.domain.user.entity;


public enum Role {
    USER("ROLE_USER", "일반 구매자"),
    SELLER("ROLE_SELLER", "판매자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;

    Role(String key, String title){
        this.key = key;
        this.title = title;
    }

    public String getKey(){
        return key;
    }
}
