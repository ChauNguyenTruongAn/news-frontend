package com.example.newswebsite.domain.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeDTO {
    private Integer categoryId;
    private String name;
    private String description;
    private Integer parentId; // ID của category cha (null nếu là root)
    private List<CategoryTreeDTO> subcategories; // Danh sách category con
}