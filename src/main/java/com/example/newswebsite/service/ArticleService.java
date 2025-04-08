package com.example.newswebsite.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Article;
import com.example.newswebsite.domain.Category;
import com.example.newswebsite.domain.ReadingHistory;
import com.example.newswebsite.domain.Tag;
import com.example.newswebsite.domain.User;
import com.example.newswebsite.domain.request.ArticleUpdateRequest;
import com.example.newswebsite.domain.response.ArticleDTO;
import com.example.newswebsite.domain.response.CategoryTreeDTO;
import com.example.newswebsite.repository.ArticleRepository;
import com.example.newswebsite.repository.CategoryRepository;
import com.example.newswebsite.repository.ReadingHistoryRepository;
import com.example.newswebsite.repository.TagRepository;
import com.example.newswebsite.repository.UserRepository;
import com.example.newswebsite.util.ArticleStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final UserRepository userService;
    private final TagRepository tagRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);

    public Page<ArticleDTO> getArticlesForHome(Pageable pageable) {
        log.info("Fetching articles for home with pageable: {}", pageable);
        Page<Article> articles = articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);
        if (articles.isEmpty()) {
            log.warn("No published articles found.");
        }
        return articles.map(this::convertToDTO);
    }

    public Page<ArticleDTO> getArticlesByCategory(Integer categoryId, Pageable pageable) {
        log.info("Fetching articles for categoryId: {} with pageable: {}", categoryId, pageable);
        Page<Article> articles = articleRepository.findByCategoryCategoryIdAndStatus(categoryId,
                ArticleStatus.PUBLISHED, pageable);
        if (articles.isEmpty()) {
            log.warn("No articles found for categoryId: {}", categoryId);
        }
        return articles.map(this::convertToDTO);
    }

    public List<CategoryTreeDTO> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::convertToTreeDTO)
                .collect(Collectors.toList());
    }

    public ArticleDTO getArticleById(Integer articleId, String googleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        if (googleId != null) {
            User user = userService.findByGoogleId(googleId);
            ReadingHistory history = new ReadingHistory();
            history.setUser(user);
            history.setArticle(article);
            history.setViewedAt(LocalDateTime.now());
            readingHistoryRepository.save(history);
        }
        return convertToDTO(article);
    }

    private ArticleDTO convertToDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        try {
            log.debug("Converting article: {}", article.getArticleId());
            dto.setArticleId(article.getArticleId());
            dto.setTitle(article.getTitle() != null ? article.getTitle() : "");
            dto.setSummary(article.getSummary() != null ? article.getSummary() : "");
            dto.setContent(article.getContent() != null ? article.getContent() : "");

            // Author
            if (article.getAuthor() != null) {
                dto.setAuthorName(article.getAuthor().getName() != null ? article.getAuthor().getName() : "Unknown");
                dto.setAuthorId(article.getAuthor().getUserId());
                dto.setAuthorGoogleId(article.getAuthor().getGoogleId());
                dto.setAuthorEmail(article.getAuthor().getEmail());
                dto.setAuthorAvatar(article.getAuthor().getAvatarUrl());
            } else {
                log.warn("Author is null for article: {}", article.getArticleId());
                dto.setAuthorName("Unknown");
                dto.setAuthorId(null);
                dto.setAuthorGoogleId(null);
                dto.setAuthorEmail(null);
                dto.setAuthorAvatar(null);
            }

            dto.setCreatedAt(article.getCreatedAt());
            dto.setStatus(article.getStatus() != null ? article.getStatus() : ArticleStatus.PENDING);

            // Category
            Category category = article.getCategory();
            if (category != null) {
                log.debug("Category found: {}", category.getCategoryId());
                ArticleDTO.CategoryDTO categoryDTO = new ArticleDTO.CategoryDTO(
                        category.getCategoryId(),
                        category.getName() != null ? category.getName() : "",
                        category.getDescription() != null ? category.getDescription() : "",
                        category.getParent() != null ? category.getParent().getCategoryId() : null);
                dto.setCategory(categoryDTO);
            } else {
                log.warn("Category is null for article: {}", article.getArticleId());
                dto.setCategory(null); // Hoặc một giá trị mặc định nếu cần
            }

            Set<Tag> tags = article.getTags();
            log.debug("Tags size: {}", tags != null ? tags.size() : "null");
            if (tags != null && !tags.isEmpty()) {
                Set<ArticleDTO.TagDTO> tagDTOs = tags.stream()
                        .map(tag -> new ArticleDTO.TagDTO(tag.getTagId(), tag.getName()))
                        .collect(Collectors.toSet());
                dto.setTags(tagDTOs);
            } else {
                dto.setTags(Collections.emptySet());
            }

            // Set thumbnail url
            dto.setThumbnailUrl(article.getThumbnailUrl());

            return dto;
        } catch (Exception e) {
            log.error("Error converting article {} to DTO: {}", article.getArticleId(), e.getMessage(), e);
            throw new RuntimeException("Failed to convert article to DTO", e);
        }
    }

    // Chuyển đổi Category sang Tree DTO
    private CategoryTreeDTO convertToTreeDTO(Category category) {
        CategoryTreeDTO dto = new CategoryTreeDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParent() != null ? category.getParent().getCategoryId() : null);

        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            List<CategoryTreeDTO> subcategories = category.getSubcategories().stream()
                    .map(this::convertToTreeDTO)
                    .collect(Collectors.toList());
            dto.setSubcategories(subcategories);
        } else {
            dto.setSubcategories(List.of());
        }

        return dto;
    }

    public Page<Article> findArticles(Specification<Article> spec, Pageable pageable) {
        return articleRepository.findAll(spec, pageable);
    }

    @Transactional
    public Article createArticle(Article article, Integer categoryId, List<String> tagNames, String googleId)
            throws Exception {
        log.info("Creating article with googleId: {}, categoryId: {}, tagNames: {}", googleId, categoryId, tagNames);

        // Tìm user theo googleId
        User author = userService.findByGoogleId(googleId);
        if (author == null) {
            log.error("User not found with googleId: {}", googleId);
            throw new RuntimeException("User not found");
        }

        // Kiểm tra quyền
        String role = author.getRole() != null ? author.getRole().getRoleName() : null;
        if (role == null || (!role.equalsIgnoreCase("editor") && !role.equalsIgnoreCase("admin"))) {
            log.warn("Unauthorized attempt by user: {}", googleId);
            throw new RuntimeException("Unauthorized: Only editors or admins can create articles");
        }

        // Gán thông tin cơ bản cho bài báo
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.now());
        article.setStatus(ArticleStatus.PENDING);

        // Gán thumbnail URL nếu có
        if (article.getThumbnailUrl() == null) {
            article.setThumbnailUrl(""); // Giá trị mặc định nếu không có
        }

        // Gán category
        if (categoryId == null) {
            log.error("Category ID is required");
            throw new RuntimeException("Category ID is required");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", categoryId);
                    return new RuntimeException("Category not found");
                });
        log.debug("Category found with ID: {}", category.getCategoryId()); // Thêm log để kiểm tra
        article.setCategory(category);

        // Gán tags
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tagsToAssign = new HashSet<>();

            for (String tagName : tagNames) {
                if (tagName != null && !tagName.trim().isEmpty()) {
                    String normalizedTagName = tagName.trim();
                    Tag tag = tagRepository.findByName(normalizedTagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(normalizedTagName);
                                return tagRepository.save(newTag);
                            });
                    tagsToAssign.add(tag);
                }
            }
            article.setTags(tagsToAssign);
        } else {
            article.setTags(Collections.emptySet());
        }

        // Lưu bài báo
        try {
            Article savedArticle = articleRepository.save(article);
            log.info("Article created successfully with ID: {}", savedArticle.getArticleId());
            return savedArticle;
        } catch (Exception e) {
            log.error("Error saving article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save article", e);
        }
    }

    public Article publishArticle(Integer articleId, String googleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        User user = userService.findByGoogleId(googleId);
        // Chỉ ADMIN được duyệt đăng
        if (!user.getRole().getRoleName().equals("admin")) {
            throw new RuntimeException("Unauthorized: Only admins can publish articles");
        }

        article.setStatus(ArticleStatus.PUBLISHED);
        return articleRepository.save(article);
    }

    public Article updateArticle(Integer articleId, ArticleUpdateRequest request, String googleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        User user = userService.findByGoogleId(googleId);

        // Cho phép ADMIN và EDITOR cập nhật
        String role = user.getRole().getRoleName();
        if (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("editor")) {
            throw new RuntimeException("Unauthorized: Only admins and editors can update articles");
        }

        // Kiểm tra quyền: EDITOR chỉ được cập nhật bài viết của mình
        if (role.equalsIgnoreCase("editor") && !article.getAuthor().getGoogleId().equals(googleId)) {
            throw new RuntimeException("Unauthorized: Editors can only update their own articles");
        }

        // Cập nhật title
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            article.setTitle(request.getTitle());
        }

        // Cập nhật summary
        if (request.getSummary() != null && !request.getSummary().isBlank()) {
            article.setSummary(request.getSummary());
        }

        // Cập nhật category
        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            Category category = categoryService.getOrCreateCategoryByName(request.getCategoryName());
            article.setCategory(category);
        }

        // Cập nhật categoryId
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            article.setCategory(category);
        }

        // Cập nhật tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = request.getTags().stream()
                    .map(name -> tagRepository.findByName(name)
                            .orElseGet(() -> tagRepository.save(new Tag(null, name, null))))
                    .collect(Collectors.toSet());
            article.setTags(tags);
        }

        // Cập nhật content (HTML)
        if (request.getContent() != null) {
            article.setContent(request.getContent()); // Lưu HTML trực tiếp
        }

        // Cập nhật thumbnailUrl
        if (request.getThumbnailUrl() != null) {
            article.setThumbnailUrl(request.getThumbnailUrl());
        }

        // Sau khi cập nhật, bài viết trở về trạng thái chờ duyệt
        article.setStatus(ArticleStatus.PENDING);
        log.info("Article {} updated by {}. Status set to PENDING", articleId, googleId);

        return articleRepository.save(article);
    }

    public void deleteArticle(Integer articleId, String googleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        User user = userService.findByGoogleId(googleId);
        if (!article.getAuthor().getGoogleId().equals(googleId) && !user.getRole().getRoleName().equals("admin")) {
            throw new RuntimeException("Unauthorized");
        }
        articleRepository.delete(article);
    }

    public Article addTagsToArticle(Integer articleId, List<String> tagNames, String googleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        User user = userService.findByGoogleId(googleId);
        if (!article.getAuthor().getGoogleId().equals(googleId) && !user.getRole().getRoleName().equals("admin")) {
            throw new RuntimeException("Unauthorized");
        }
        if (article.getTags() == null) {
            article.setTags(new HashSet<>());
        }
        Set<Tag> tags = tagNames.stream()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(null, name, null))))
                .collect(Collectors.toSet());
        article.getTags().addAll(tags);
        return articleRepository.save(article);
    }

    // public Article getArticleById(Integer articleId, String googleId) {
    // Article article = articleRepository.findById(articleId)
    // .orElseThrow(() -> new RuntimeException("Article not found"));

    // if (googleId != null) {
    // User user = userService.findByGoogleId(googleId);
    // ReadingHistory history = new ReadingHistory();
    // history.setUser(user);
    // history.setArticle(article);
    // history.setViewedAt(LocalDateTime.now());
    // readingHistoryRepository.save(history);
    // }

    // return article;
    // }

    public List<ReadingHistory> getReadingHistory(String googleId) {
        return readingHistoryRepository.findByUserGoogleIdOrderByViewedAtDesc(googleId);
    }

    public Page<ArticleDTO> getArticlesByAuthorGoogleId(String googleId, Pageable pageable) {
        log.info("Fetching articles for author with googleId: {} with pageable: {}", googleId, pageable);
        Page<Article> articles = articleRepository.findByAuthorGoogleIdAndStatus(googleId, ArticleStatus.PUBLISHED,
                pageable);
        if (articles.isEmpty()) {
            log.warn("No published articles found for author with googleId: {}", googleId);
        }
        return articles.map(this::convertToDTO);
    }

    /**
     * Lấy bài viết đã được xuất bản theo ID
     * 
     * @param articleId ID của bài viết
     * @param googleId  Google ID của người đọc (để ghi lịch sử đọc)
     * @return Chi tiết bài viết đã xuất bản, hoặc null nếu bài viết không tồn tại
     *         hoặc chưa xuất bản
     */
    public ArticleDTO getPublishedArticleById(Integer articleId, String googleId) {
        log.info("Fetching published article with id: {}", articleId);
        Article article = articleRepository.findById(articleId)
                .orElse(null);

        if (article == null || article.getStatus() != ArticleStatus.PUBLISHED) {
            log.warn("Article not found or not published: {}", articleId);
            return null;
        }

        // Ghi lịch sử đọc nếu có thông tin người dùng
        if (googleId != null) {
            try {
                User user = userService.findByGoogleId(googleId);
                if (user != null) {
                    ReadingHistory history = new ReadingHistory();
                    history.setUser(user);
                    history.setArticle(article);
                    history.setViewedAt(LocalDateTime.now());
                    readingHistoryRepository.save(history);
                    log.debug("Reading history saved for user: {} and article: {}", googleId, articleId);
                }
            } catch (Exception e) {
                log.error("Error saving reading history: {}", e.getMessage(), e);
                // Không ném exception để không làm gián đoạn việc đọc bài viết
            }
        }

        return convertToDTO(article);
    }

    /**
     * Lấy tất cả bài viết không phân biệt trạng thái và chuyển đổi thành DTO
     * 
     * @param pageable Thông tin phân trang
     * @return Danh sách bài viết dạng DTO với phân trang
     */
    public Page<ArticleDTO> findAllArticlesAsDTO(Pageable pageable) {
        log.info("Fetching all articles with pageable: {}", pageable);
        Page<Article> articles = articleRepository.findAll(pageable);
        return articles.map(this::convertToDTO);
    }

    @Transactional
    public Article updateArticleStatus(Integer articleId, ArticleStatus status) {
        log.info("Updating article {} status to {}", articleId, status);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        article.setStatus(status);
        return articleRepository.save(article);
    }

    public Page<Article> getLatestArticles(Pageable pageable) {
        log.info("Getting latest articles");
        return articleRepository.findLatestArticles(ArticleStatus.PUBLISHED, pageable);
    }

    public Page<Article> getHotArticles(Pageable pageable) {
        log.info("Getting hot articles");
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        return articleRepository.findHotArticles(ArticleStatus.PUBLISHED, startDate, pageable);
    }
}
