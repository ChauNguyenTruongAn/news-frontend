package com.example.newswebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.newswebsite.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByGoogleId(String googleId);

    User findByEmail(String email);
}