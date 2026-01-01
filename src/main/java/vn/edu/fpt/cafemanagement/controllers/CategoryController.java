package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.repositories.CategoryRepository;
import vn.edu.fpt.cafemanagement.services.CategoryService;

@Controller
@RequestMapping(value = "/category")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private static final String VIETNAMESE_NAME_PATTERN = "^[\\p{L}\\s]+$";

    @GetMapping(value = "/list")
    public String showCategoryList(Model model) {

        model.addAttribute("CategoryList", categoryService.getCategories());
        return "category/list";
    }

    @GetMapping(value = "/create")
    public String showCreateForm(Model model) {
        model.addAttribute("title", "Create Category");
        model.addAttribute("category", new Category());
        return "/category/create";
    }

    @GetMapping(value = "/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {

        Integer idInt = null;
        try {
            idInt = Integer.parseInt(id);
        } catch (NumberFormatException e) {

            System.err.println("ID nhập vào không phải là số: " + id);
            idInt = -1;
        }
        model.addAttribute("title", "Edit Category");

        model.addAttribute("category", categoryService.getCategoryById(idInt));

        return "category/edit";
    }

    @GetMapping(value = "/deleted-list")
    public String viewDeletedCategories(Model model) {
        model.addAttribute("title", "Category List");
        model.addAttribute("categoryList", categoryService.getNonActiveCategories());
        return "category/deleted-list";
    }

    @PostMapping(value = "/edit/{id}")
    public String editCategory(@ModelAttribute("category") Category category,
                               @PathVariable("id") int cateId, Model model) {
        boolean hasError = false;


        if (cateId != category.getCateId()) {
            System.err.println("SECURITY ALERT: Product ID mismatch. URL ID: " + cateId + ", Form ID: " + category.getCateId());

            // Trả về trang lỗi
            model.addAttribute("title", "Lỗi Bảo Mật Dữ Liệu");
            model.addAttribute("errorMessage", "Thông tin ID sản phẩm không nhất quán. Yêu cầu của bạn đã bị từ chối để đảm bảo an toàn hệ thống.");
            return "error-page"; // Trả về error-page.html
        }
        Category originalCategory = categoryService.getCategoryById(cateId);

        // Validation Name
        String proName = category.getCateName();
        if (proName == null || proName.trim().isEmpty()) {
            model.addAttribute("nameError", "Category name cant be empty");
            hasError = true;
        } else if (!proName.matches(VIETNAMESE_NAME_PATTERN)) {
            model.addAttribute("nameError", "Name just can contain letters!");
            hasError = true;
        }
        if (hasError) {

            // model.addAttribute("categoryList", categoryService.findAll());
            return "/category/edit";
        }
        originalCategory.setCateName(category.getCateName());
        categoryService.saveCategory(category);
        return "redirect:/category/list";
    }

    @PostMapping(value = "/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, Model model) {
        categoryService.deleteSortCategory(categoryService.getCategoryById(id));
        return "redirect:/category/list";
    }

    @PostMapping(value = "/create")
    public String createCategory(@ModelAttribute("category") Category category, Model model) {
        boolean hasError = false;
        category.setCateId(0);
        // Validation Name
        String proName = category.getCateName();
        if (proName == null || proName.trim().isEmpty()) {
            model.addAttribute("nameError", "Category name cant be empty");
            hasError = true;
        } else if (!proName.matches(VIETNAMESE_NAME_PATTERN)) {
            model.addAttribute("nameError", "Name just can contain letters!");
            hasError = true;
        }
        if (hasError) {
            // Đảm bảo truyền lại danh sách category nếu có lỗi

            return "/category/create";
        }
        category.setActive(true);
        categoryService.saveCategory(category);
        return "redirect:/category/list";
    }
}
