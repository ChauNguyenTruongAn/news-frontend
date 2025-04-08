package com.example.newswebsite.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.cors(cors -> cors.configurationSource(request -> {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:5500",
                                        "https://stunews.static.domains"));
                        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                        config.setAllowedHeaders(Arrays.asList("*"));
                        config.setAllowCredentials(true);
                        return config;
                })).csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints - không cần xác thực
                                                .requestMatchers("/api/auth/**", "/api/auth/google-callback",
                                                                "/api/articles")
                                                .permitAll()
                                                .requestMatchers("/callback").permitAll()
                                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .requestMatchers("/upload-image", "/export-html").permitAll()

                                                // Public article endpoints
                                                .requestMatchers(HttpMethod.GET, "/api/articles/home",
                                                                "api/articles/hot", "/api/articles/latest")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/articles/{id:[0-9]+}")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/articles/category/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/tags").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/comments/article/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                                                // User endpoints - cần đăng nhập
                                                .requestMatchers(HttpMethod.GET, "/api/articles/author/**")
                                                .hasAnyRole("EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/articles")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/articles/{id:[0-9]+}")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/articles/{id:[0-9]+}")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/comments")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/comments/**")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/comments/**")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/articles/history")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/reading-history/**")
                                                .hasAnyRole("USER", "EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/users/request-editor")
                                                .hasRole("USER")

                                                // Editor endpoints
                                                .requestMatchers(HttpMethod.POST, "/api/articles/publish")
                                                .hasAnyRole("EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/articles/status/**")
                                                .hasAnyRole("EDITOR", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/articles/**")
                                                .hasAnyRole("EDITOR", "ADMIN")
                                                // Admin endpoints
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/categories/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/users/approve-editor")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/stats").hasRole("ADMIN")

                                                // Yêu cầu xác thực cho các endpoint còn lại
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}

// .oauth2Login(oauth2 -> oauth2
// .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new
// OidcUserService()))
// .successHandler((request, response, authentication) -> {
// System.out.println("OAuth Success - Principal: " +
// authentication.getPrincipal());
// OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
// User user = userService.saveOrUpdateUser(oidcUser);
// String accessToken = jwtService.generateToken(user.getGoogleId(),
// user.getRole().getRoleName());
// String refreshToken =
// refreshTokenService.createRefreshToken(user).getToken();

// // Thêm access token vào cookie
// Cookie jwtCookie = new Cookie("jwt", accessToken);
// jwtCookie.setHttpOnly(true);
// jwtCookie.setPath("/");
// jwtCookie.setMaxAge(60 * 60); // 1 giờ
// response.addCookie(jwtCookie);

// // Trả JSON trực tiếp
// response.setContentType("application/json");
// String jsonResponse = String.format(
// "{\"message\": \"Login successful\", \"access_token\": \"%s\",
// \"refresh_token\": \"%s\"}",
// accessToken, refreshToken);
// response.getWriter().write(jsonResponse);
// })
// .failureHandler((request, response, exception) -> {
// System.out.println("OAuth Failure: " + exception.getMessage());
// response.setContentType("application/json");
// response.setStatus(401);
// response.getWriter().write("{\"error\": \"OAuth login failed\", \"message\":
// \""
// + exception.getMessage() + "\"}");
// }))