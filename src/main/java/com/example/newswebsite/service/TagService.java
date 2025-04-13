package com.example.newswebsite.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.newswebsite.domain.Tag;
import com.example.newswebsite.repository.ArticleRepository;
import com.example.newswebsite.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {
    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;

    public List<Tag> getAllTags() {
        log.info("Getting all tags");
        return tagRepository.findAll();
    }

    public Tag createTag(String name) {
        log.info("Creating new tag: {}", name);
        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Integer tagId) {
        log.info("Deleting tag with id: {}", tagId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        // Xóa tag khỏi tất cả các bài viết
        tag.getArticles().forEach(article -> {
            article.getTags().remove(tag);
            articleRepository.save(article);
        });

        // Xóa tag
        tagRepository.delete(tag);
    }

    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }
}