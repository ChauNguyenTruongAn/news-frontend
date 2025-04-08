package com.example.newswebsite.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Tag;
import com.example.newswebsite.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Tag createTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(null, name, null)));
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }
}