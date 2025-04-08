package com.example.newswebsite.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.newswebsite.domain.Category;
import com.example.newswebsite.domain.request.CategoryRequest;
import com.example.newswebsite.domain.response.CategoryTreeDTO;
import com.example.newswebsite.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category findById(Integer id) throws Exception {
        return categoryRepository.findById(id).orElseThrow(() -> new Exception("Not found category: " + id));
    }

    public List<Category> findByParentIsNull() {
        return categoryRepository.findByParentIsNull();
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category getOrCreateCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription("");
                    return categoryRepository.save(category);
                });
    }

    /**
     * Lấy danh sách các danh mục con dựa theo ID của danh mục cha
     * 
     * @param parentId ID của danh mục cha
     * @return Danh sách các danh mục con
     */
    public List<Category> findChildCategories(Integer parentId) {
        log.info("Finding child categories for parent ID: {}", parentId);
        return categoryRepository.findByParentCategoryId(parentId);
    }

    /**
     * Lấy cấu trúc cây đầy đủ của tất cả danh mục
     * 
     * @return Danh sách các danh mục dạng cây (chỉ 2 cấp: cha và con)
     */
    public List<CategoryTreeDTO> getCategoryTree() {
        log.info("Getting complete category tree");
        List<Category> parentCategories = findByParentIsNull();

        return parentCategories.stream()
                .map(this::convertToCategoryTreeDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Category thành CategoryTreeDTO
     * 
     * @param category Đối tượng Category
     * @return CategoryTreeDTO với thông tin về danh mục con
     */
    private CategoryTreeDTO convertToCategoryTreeDTO(Category category) {
        CategoryTreeDTO dto = new CategoryTreeDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParent() != null ? category.getParent().getCategoryId() : null);

        // Lấy danh mục con nếu là danh mục cha
        if (category.getParent() == null) {
            List<Category> childCategories = findChildCategories(category.getCategoryId());
            List<CategoryTreeDTO> childDtos = childCategories.stream()
                    .map(child -> {
                        CategoryTreeDTO childDto = new CategoryTreeDTO();
                        childDto.setCategoryId(child.getCategoryId());
                        childDto.setName(child.getName());
                        childDto.setDescription(child.getDescription());
                        childDto.setParentId(category.getCategoryId());
                        // Danh mục con không có danh mục con của nó (giới hạn 2 cấp)
                        childDto.setSubcategories(List.of());
                        return childDto;
                    })
                    .collect(Collectors.toList());

            dto.setSubcategories(childDtos);
        } else {
            dto.setSubcategories(List.of());
        }

        return dto;
    }

    /**
     * Tạo danh mục cha mới (không có parent)
     * 
     * @param name        Tên danh mục
     * @param description Mô tả danh mục
     * @return Danh mục cha đã được tạo
     */
    public Category createParentCategory(String name, String description) {
        log.info("Creating parent category with name: {}", name);

        Category category = new Category();
        category.setName(name);
        category.setDescription(description != null ? description : "");
        category.setParent(null);

        return categoryRepository.save(category);
    }

    /**
     * Tạo danh mục con với danh mục cha được chỉ định
     * 
     * @param name        Tên danh mục
     * @param description Mô tả danh mục
     * @param parentId    ID của danh mục cha
     * @return Danh mục con đã được tạo
     * @throws Exception Nếu không tìm thấy danh mục cha
     */
    public Category createChildCategory(String name, String description, Integer parentId) throws Exception {
        log.info("Creating child category with name: {} and parentId: {}", name, parentId);

        // Tìm danh mục cha
        Category parent = findById(parentId);

        // Tạo danh mục con
        Category childCategory = new Category();
        childCategory.setName(name);
        childCategory.setDescription(description != null ? description : "");
        childCategory.setParent(parent);

        return categoryRepository.save(childCategory);
    }

    /**
     * Xóa danh mục theo ID
     * 
     * @param categoryId ID của danh mục cần xóa
     * @throws Exception Nếu danh mục không tồn tại hoặc không thể xóa
     */
    public void deleteCategory(Integer categoryId) throws Exception {
        log.info("Deleting category with ID: {}", categoryId);

        Category category = findById(categoryId);

        // Kiểm tra xem danh mục có danh mục con hay không
        List<Category> childCategories = findChildCategories(categoryId);
        if (!childCategories.isEmpty()) {
            log.warn("Cannot delete category with ID: {} because it has child categories", categoryId);
            throw new Exception("Không thể xóa danh mục này vì nó có chứa danh mục con");
        }

        // Xóa danh mục
        categoryRepository.delete(category);
    }

    /**
     * Cập nhật danh mục theo ID
     * 
     * @param categoryId ID của danh mục cần cập nhật
     * @param request    Thông tin cập nhật danh mục
     * @return Danh mục đã được cập nhật
     * @throws Exception Nếu danh mục không tồn tại
     */
    public Category updateCategory(Integer categoryId, CategoryRequest request) throws Exception {
        log.info("Updating category with ID: {} - Name: {}", categoryId, request.getName());

        // Tìm danh mục hiện tại
        Category category = findById(categoryId);

        // Cập nhật thông tin danh mục
        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        // Cập nhật danh mục cha nếu có
        if (request.getParentId() != null) {
            // Nếu parentId là 0, đặt parent là null (biến thành danh mục cha)
            if (request.getParentId() == 0) {
                category.setParent(null);
            } else {
                // Tìm danh mục cha mới
                Category parent = findById(request.getParentId());

                // Kiểm tra chu trình: không cho phép danh mục cha là chính nó hoặc là con của
                // nó
                if (request.getParentId().equals(categoryId)) {
                    throw new Exception("Không thể đặt danh mục làm cha của chính nó");
                }

                // Kiểm tra xem danh mục cha mới có phải là con của danh mục hiện tại không
                List<Category> childCategories = findChildCategories(categoryId);
                if (childCategories.stream().anyMatch(child -> child.getCategoryId().equals(request.getParentId()))) {
                    throw new Exception("Không thể đặt danh mục con làm danh mục cha");
                }

                category.setParent(parent);
            }
        }

        return categoryRepository.save(category);
    }
}