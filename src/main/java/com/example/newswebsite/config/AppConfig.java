package com.example.newswebsite.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
    // Các URL API
    public static final String API_BASE_URL = "/api";
    public static final String API_ARTICLES_URL = API_BASE_URL + "/articles";
    public static final String API_TAGS_URL = API_BASE_URL + "/tags";
    public static final String API_CATEGORIES_URL = API_BASE_URL + "/categories";
    public static final String API_COMMENTS_URL = API_BASE_URL + "/comments";
    public static final String API_SEARCH_URL = API_BASE_URL + "/search";
    public static final String API_STATS_URL = API_BASE_URL + "/stats";
    public static final String API_USERS_URL = API_BASE_URL + "/users";
    public static final String API_AUTH_URL = API_BASE_URL + "/auth";

    // Các endpoint cụ thể
    public static final String ENDPOINT_LATEST = "/latest";
    public static final String ENDPOINT_HOT = "/hot";
    public static final String ENDPOINT_CATEGORY = "/category";
    public static final String ENDPOINT_COMMENTS = "/comments";
    public static final String ENDPOINT_REPLIES = "/replies";
    public static final String ENDPOINT_FAVORITES = "/favorites";
    public static final String ENDPOINT_HISTORY = "/history";
    public static final String ENDPOINT_LOGIN = "/login";
    public static final String ENDPOINT_REGISTER = "/register";
    public static final String ENDPOINT_LOGOUT = "/logout";
    public static final String ENDPOINT_REFRESH = "/refresh";

    // Các thông tin deploy
    public static final String APP_NAME = "News Website";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_DESCRIPTION = "A modern news website with full features";
    public static final String APP_CONTACT_NAME = "Admin";
    public static final String APP_CONTACT_EMAIL = "admin@example.com";
    public static final String APP_CONTACT_URL = "https://example.com";

    // Các thông tin bảo mật
    public static final String JWT_SECRET = "your-secret-key";
    public static final long JWT_EXPIRATION = 86400000; // 24 hours
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // Các thông tin database
    public static final String DB_URL = "jdbc:mysql://localhost:3306/news_web";
    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "password";
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_DIALECT = "org.hibernate.dialect.MySQL8Dialect";

    // Các thông tin upload file
    public static final String UPLOAD_DIR = "uploads";
    public static final String ALLOWED_IMAGE_TYPES = "image/jpeg,image/png,image/gif";
    public static final long MAX_FILE_SIZE = 5242880; // 5MB

    // Các thông tin cache
    public static final String CACHE_ARTICLES = "articles";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_TAGS = "tags";
    public static final long CACHE_EXPIRATION = 3600; // 1 hour
}