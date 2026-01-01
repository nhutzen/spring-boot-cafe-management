package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.entities.Category;
import vn.edu.fpt.cafemanagement.entities.Product;
import vn.edu.fpt.cafemanagement.services.CategoryService;
import vn.edu.fpt.cafemanagement.services.ProductService;

import java.util.List;

@Controller
@RequestMapping("/home")
public class MenuController {
    CategoryService categoryService;
    ProductService productService;

    public MenuController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

//    @GetMapping("/menu")
//    public String showMenuPage(Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
//        int pageSize = 10;
//        Pageable pageable = PageRequest.of(page - 1, pageSize);
//        Page<Product> productPage = productService.getAllProductsPage(pageable);
//        List<Category> categoryList = categoryService.getCategories();
//        model.addAttribute("productList", productPage.getContent());
//        model.addAttribute("categoryList", categoryList);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", productPage.getTotalPages());
//        return "home/home";
//    }

    @GetMapping(value = {"", "/search"})
    public String showMenu(
            // NHẬN DƯỚI DẠNG STRING để bắt lỗi người dùng nhập chữ
            @RequestParam(value = "categoryId", required = false) String categoryIdStr,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        model.addAttribute("title", "Product List");
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> list = productService.getAllProductsPaged(pageable);

        if (page > list.getTotalPages()) {
            page = list.getTotalPages();
            pageable = PageRequest.of(page - 1, size);
            list = productService.getAllProductsPaged(pageable);

        }


        // BƯỚC 1: XỬ LÝ VÀ CHUYỂN ĐỔI THAM SỐ (Bắt lỗi nhập chữ)
        Integer tempCategoryId = 0;
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            try {
                tempCategoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                System.err.println("Cảnh báo bảo mật: categoryId không phải là số. Mặc định về 0.");
                tempCategoryId = 0;
            }
        }

        // TẠO BIẾN FINAL: Đây là giá trị cuối cùng được sử dụng trong lambda (để tránh lỗi)
        final Integer finalCategoryId = tempCategoryId;

        // BƯỚC 2: KIỂM TRA TÍNH HỢP LỆ CỦA ID (Bắt lỗi thao túng ID không tồn tại)
        List<Category> categoryList = categoryService.getCategories();

        // Kiểm tra: ID phải là ID hợp lệ, hoặc là ID = 0 (All Categories)
        boolean isValidCategory = categoryList.stream()
                .anyMatch(c -> c.getCateId() == finalCategoryId) || finalCategoryId == 0;

        // BƯỚC 3: LỌC SẢN PHẨM
        Integer categoryIdForModel = finalCategoryId; // Biến này dùng để truyền về View

        if (finalCategoryId > 0 && isValidCategory) {
            list = productService.getProductsByCategory(finalCategoryId, pageable);
        } else {
            // Mặc định cho ID = 0, ID < 0, ID không hợp lệ
            list = productService.getAllProductsPaged(pageable);
            categoryIdForModel = 0;
        }

        // --- BƯỚC 2: QUYẾT ĐỊNH LỌC/TÌM KIẾM ---

        // 1. Chuẩn hóa keyword
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null; // Coi chuỗi rỗng là không tìm kiếm
        }

        if (keyword != null) {
            // TRƯỜNG HỢP A: ĐANG TÌM KIẾM THEO KEYWORD

            if (finalCategoryId > 0 && isValidCategory) {
                // Lọc theo Category VÀ Keyword
                // Lưu ý: Bạn cần một service mới hỗ trợ cả hai tham số này
                list = productService.searchProductsByCategoryAndKeyword(finalCategoryId, keyword, pageable);

            } else {
                // Chỉ tìm kiếm theo Keyword (Category = All)
                list = productService.getSearchProducts(keyword, pageable);
                categoryIdForModel = 0; // Đảm bảo filter category hiển thị 'All'
            }

        } else {
            // TRƯỜNG HỢP B: KHÔNG CÓ KEYWORD (Chỉ lọc Category hoặc All)

            if (finalCategoryId > 0 && isValidCategory) {
                // Chỉ lọc theo Category
                list = productService.getActiveProductsByCategory(finalCategoryId, pageable);
            } else {
                // Mặc định: Lấy tất cả sản phẩm
                list = productService.getAllProductsPaged(pageable);
                categoryIdForModel = 0;
            }
        }

        // BƯỚC 4: TRUYỀN DỮ LIỆU VỀ VIEW

        model.addAttribute("pageProduct", list);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("selectedCategoryId", categoryIdForModel);
        model.addAttribute("keyword", keyword); // Truyền keyword về view
        return "home/home";
    }

//    @GetMapping(value = {"/list", "/search"}) // Cả hai URL đều dẫn đến đây
//    public String listProducts(
//            @RequestParam(value = "categoryId", required = false) String categoryIdStr,
//            @RequestParam(value = "keyword", required = false) String keyword,
//            @RequestParam(name = "page", defaultValue = "1") int page,
//            Model model) {
//
//        model.addAttribute("title", "Product List");
//        int size = 10;
//        if (page < 1) {
//            page = 1;
//        }
//
//        Pageable pageable = PageRequest.of(page - 1, size);
//        Page<Product> list;
//
//        // --- BƯỚC 1: XỬ LÝ categoryIdStr (NHƯ CODE CŨ CỦA BẠN) ---
//        Integer tempCategoryId = 0;
//        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
//            try {
//                tempCategoryId = Integer.parseInt(categoryIdStr);
//            } catch (NumberFormatException e) {
//                System.err.println("Cảnh báo bảo mật: categoryId không phải là số. Mặc định về 0.");
//                tempCategoryId = 0;
//            }
//        }
//        final Integer finalCategoryId = tempCategoryId;
//
//        // Lấy danh sách Categories để kiểm tra hợp lệ
//        List<Category> categoryList = categoryService.getCategories();
//        boolean isValidCategory = categoryList.stream().anyMatch(c -> c.getCateId() == finalCategoryId) || finalCategoryId == 0;
//
//        Integer categoryIdForModel = finalCategoryId; // Biến này dùng để truyền về View
//
//
//        // --- BƯỚC 2: QUYẾT ĐỊNH LỌC/TÌM KIẾM ---
//
//        // 1. Chuẩn hóa keyword
//        if (keyword != null && keyword.trim().isEmpty()) {
//            keyword = null; // Coi chuỗi rỗng là không tìm kiếm
//        }
//
//        if (keyword != null) {
//            // TRƯỜNG HỢP A: ĐANG TÌM KIẾM THEO KEYWORD
//
//            if (finalCategoryId > 0 && isValidCategory) {
//                // Lọc theo Category VÀ Keyword
//                // Lưu ý: Bạn cần một service mới hỗ trợ cả hai tham số này
//                list = productService.searchProductsByCategoryAndKeyword(finalCategoryId, keyword, pageable);
//
//            } else {
//                // Chỉ tìm kiếm theo Keyword (Category = All)
//                list = productService.getSearchProducts(keyword, pageable);
//                categoryIdForModel = 0; // Đảm bảo filter category hiển thị 'All'
//            }
//
//        } else {
//            // TRƯỜNG HỢP B: KHÔNG CÓ KEYWORD (Chỉ lọc Category hoặc All)
//
//            if (finalCategoryId > 0 && isValidCategory) {
//                // Chỉ lọc theo Category
//                list = productService.getActiveProductsByCategory(finalCategoryId, pageable);
//            } else {
//                // Mặc định: Lấy tất cả sản phẩm
//                list = productService.getAllProductsPaged(pageable);
//                categoryIdForModel = 0;
//            }
//        }
//
//
//        // --- BƯỚC 3: XỬ LÝ PHÂN TRANG (CẦN ĐẶT SAU KHI LẤY LIST) ---
//        if (list.getTotalPages() > 0 && page > list.getTotalPages()) {
//            page = list.getTotalPages();
//            pageable = PageRequest.of(page - 1, size);
//
//            // Cần gọi lại service tương ứng với logic đã chọn
//            // Cần tinh chỉnh logic này để tránh gọi lại nhiều lần và phức tạp
//            // Dễ hơn là bạn chỉ cần kiểm tra page < 1 ở đầu và để Thymeleaf xử lý
//            // Nhưng nếu muốn fix page ở đây:
//
//            // (Bỏ qua việc gọi lại ở đây cho gọn, chỉ cần đảm bảo Thymeleaf xử lý số trang đúng)
//        }
//
//        // --- BƯỚC 4: TRUYỀN DỮ LIỆU VỀ VIEW ---
//        model.addAttribute("pageProduct", list);
//        model.addAttribute("categoryList", categoryList);
//        model.addAttribute("selectedCategoryId", categoryIdForModel);
//        model.addAttribute("keyword", keyword); // Truyền keyword về view
//
//        return "product/list";
//    }


}
