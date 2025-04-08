package com.example.newswebsite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "petrol-user",
                "api_key", "588188343441269",
                "api_secret", "PxRV89g2JdJH3WlnDh_LZfErGMw"));

    }
}
