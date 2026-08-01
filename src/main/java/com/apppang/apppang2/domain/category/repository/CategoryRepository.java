package com.apppang.apppang2.domain.category.repository;

import com.apppang.apppang2.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
