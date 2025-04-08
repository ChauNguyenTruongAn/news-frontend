package com.example.newswebsite.service;

import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.stereotype.Service;

@Service
public class OidcuserService {

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }
}
