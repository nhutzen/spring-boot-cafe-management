import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import vn.edu.fpt.cafemanagement.controllers.CategoryController;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.services.CategoryService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    @Mock
    private Model model;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // Giả định regex chỉ cho phép chữ và khoảng trắng
    private static final String VIETNAMESE_NAME_PATTERN = "^[\\p{L}\\s]+$";

    @Test
    void testUTCID01_nullName() {
        Category category = new Category();
        category.setCateName(null);

        String result = categoryController.createCategory(category, model);

        assertEquals("/category/create", result);
        verify(model).addAttribute("nameError", "Category name cant be empty");
        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void testUTCID02_emptyString() {
        Category category = new Category();
        category.setCateName("");

        String result = categoryController.createCategory(category, model);

        assertEquals("/category/create", result);
        verify(model).addAttribute("nameError", "Category name cant be empty");
    }

    @Test
    void testUTCID03_spacesOnly() {
        Category category = new Category();
        category.setCateName("   ");

        String result = categoryController.createCategory(category, model);

        assertEquals("/category/create", result);
        verify(model).addAttribute("nameError", "Category name cant be empty");
        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void testUTCID04_invalidName_digits() {
        Category category = new Category();
        category.setCateName("Coffee 123");

        String result = categoryController.createCategory(category, model);

        assertEquals("/category/create", result);
        verify(model).addAttribute("nameError", "Name just can contain letters!");
        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void testUTCID05_invalidName_symbol() {
        Category category = new Category();
        category.setCateName("Tea @");

        String result = categoryController.createCategory(category, model);

        assertEquals("/category/create", result);
        verify(model).addAttribute("nameError", "Name just can contain letters!");
        verify(categoryService, never()).saveCategory(any());
    }

    @Test
    void testUTCID06_validName1() {
        Category category = new Category();
        category.setCateName("Milk tea");

        String result = categoryController.createCategory(category, model);

        assertEquals("redirect:/category/list", result);
        verify(categoryService, times(1)).saveCategory(any());
    }

    @Test
    void testUTCID07_validName2() {
        Category category = new Category();
        category.setCateName("COLDBREW");

        String result = categoryController.createCategory(category, model);

        assertEquals("redirect:/category/list", result);
        verify(categoryService, times(1)).saveCategory(any());
    }

    @Test
    void testUTCID08_validNameWithSpaces() {
        Category category = new Category();
        category.setCateName("   coldbrew");

        String result = categoryController.createCategory(category, model);

        assertEquals("redirect:/category/list", result);
        verify(categoryService, times(1)).saveCategory(any());
    }
}