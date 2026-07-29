package com.apppang.apppang2.domain.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    //상품의 category_id와 번호를 맞추기 위해 고정 번호 사용
    @Id
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;
}