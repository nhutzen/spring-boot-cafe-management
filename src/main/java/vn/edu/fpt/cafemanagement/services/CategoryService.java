package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.entities.Product;
import vn.edu.fpt.cafemanagement.repositories.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {

    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public List<Category> getNonActiveCategories() {
        return categoryRepository.findByIsActiveFalse();
    }

    public Category getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId).orElse(null);
    }

    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    public void deleteSortCategory(Category category) {
        category.setActive(false);
        categoryRepository.save(category);
    }

}