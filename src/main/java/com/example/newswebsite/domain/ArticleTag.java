// package com.example.newswebsite.domain;

// import jakarta.persistence.Entity;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Entity
// @Table(name = "ArticleTags")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class ArticleTag {
//     @Id
//     @ManyToOne
//     @JoinColumn(name = "article_id")
//     private Article article;

//     @Id
//     @ManyToOne
//     @JoinColumn(name = "tag_id")
//     private Tag tag;
// }