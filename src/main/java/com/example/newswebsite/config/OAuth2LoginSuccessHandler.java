package com.example.newswebsite.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.newswebsite.domain.RefreshToken;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.service.JwtService;
import com.example.newswebsite.service.RefreshTokenService;
import com.example.newswebsite.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        User user = userService.saveOrUpdateUser(oidcUser);

        // Tạo tokens
        String accessToken = jwtService.generateToken(user.getGoogleId(), user.getRole().getRoleName());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Xử lý theo loại response
        String responseType = request.getParameter("response_type");

        if ("token".equals(responseType)) {
            // Trả về token dưới dạng JSON
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format("{\"access_token\": \"%s\", \"refresh_token\": \"%s\"}",
                            accessToken,
                            refreshToken.getToken()));
        } else {
            // Set cookie và redirect về trang chủ
            setTokenCookies(response, accessToken, refreshToken.getToken());
            response.sendRedirect("/"); // Trang chủ
        }
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Cấu hình cookie cho access token
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(3600); // 1 giờ

        // Cấu hình cookie cho refresh token
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800); // 7 ngày

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
}