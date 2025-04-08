package com.example.newswebsite.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.newswebsite.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);

    List<Category> findByParentIsNull();

    List<Category> findByParentCategoryId(Integer parentId);
}