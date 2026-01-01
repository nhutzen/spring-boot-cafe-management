package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.entities.*;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.CategoryService;
import vn.edu.fpt.cafemanagement.services.FeedbackService;
import vn.edu.fpt.cafemanagement.services.OrderItemService;
import vn.edu.fpt.cafemanagement.services.ProductService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/product")
public class ProductController {

    private ProductService productService;
    private CategoryService categoryService;
    private FeedbackService feedbackService;
    private OrderItemService orderItemService;

    public ProductController(ProductService productService, CategoryService categoryService, FeedbackService feedbackService, OrderItemService orderItemService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.feedbackService = feedbackService;
        this.orderItemService = orderItemService;
    }

    private final String UPLOAD_DIR = "D:/SWP/Project/uploads/";

    @Autowired
    private LoggedUser loggedUser;

    private static final String VIETNAMESE_NAME_PATTERN = "^[\\p{L}\\s]+$";

    @GetMapping(value = {"/list", "/search"}) // Cả hai URL đều dẫn đến đây
    public String listProducts(
            @RequestParam(value = "categoryId", required = false) String categoryIdStr,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        model.addAttribute("title", "Product List");
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> list;

        // --- BƯỚC 1: XỬ LÝ categoryIdStr (NHƯ CODE CŨ CỦA BẠN) ---
        Integer tempCategoryId = 0;
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            try {
                tempCategoryId = Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                System.err.println("Cảnh báo bảo mật: categoryId không phải là số. Mặc định về 0.");
                tempCategoryId = 0;
            }
        }
        final Integer finalCategoryId = tempCategoryId;

        // Lấy danh sách Categories để kiểm tra hợp lệ
        List<Category> categoryList = categoryService.getCategories();
        boolean isValidCategory = categoryList.stream().anyMatch(c -> c.getCateId() == finalCategoryId) || finalCategoryId == 0;

        Integer categoryIdForModel = finalCategoryId; // Biến này dùng để truyền về View


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
                productService.updateStatusForZeroQuantity(list.getContent());
                categoryIdForModel = 0; // Đảm bảo filter category hiển thị 'All'
            }

        } else {
            // TRƯỜNG HỢP B: KHÔNG CÓ KEYWORD (Chỉ lọc Category hoặc All)

            if (finalCategoryId > 0 && isValidCategory) {
                // Chỉ lọc theo Category
                list = productService.getActiveProductsByCategory(finalCategoryId, pageable);
                productService.updateStatusForZeroQuantity(list.getContent());
            } else {
                // Mặc định: Lấy tất cả sản phẩm
                list = productService.getAllProductsPaged(pageable);
                productService.updateStatusForZeroQuantity(list.getContent());
                categoryIdForModel = 0;
            }
        }


        // --- BƯỚC 3: XỬ LÝ PHÂN TRANG (CẦN ĐẶT SAU KHI LẤY LIST) ---
        if (list.getTotalPages() > 0 && page > list.getTotalPages()) {
            page = list.getTotalPages();
            pageable = PageRequest.of(page - 1, size);

            // Cần gọi lại service tương ứng với logic đã chọn
            // Cần tinh chỉnh logic này để tránh gọi lại nhiều lần và phức tạp
            // Dễ hơn là bạn chỉ cần kiểm tra page < 1 ở đầu và để Thymeleaf xử lý
            // Nhưng nếu muốn fix page ở đây:

            // (Bỏ qua việc gọi lại ở đây cho gọn, chỉ cần đảm bảo Thymeleaf xử lý số trang đúng)
        }

        // --- BƯỚC 4: TRUYỀN DỮ LIỆU VỀ VIEW ---

        model.addAttribute("pageProduct", list);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("selectedCategoryId", categoryIdForModel);
        model.addAttribute("keyword", keyword); // Truyền keyword về view

        return "product/list";
    }

    @GetMapping(value = "/deleted-list")
    public String showDeletedList(
            // Chấp nhận categoryId là String để bắt lỗi nhập chữ, hoặc Integer tùy thuộc vào cách bạn xử lý
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        model.addAttribute("title", "Deleted Product List");

        model.addAttribute("isDeletedList", true);
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> list;

        Integer tempCategoryId = 0;
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            try {
                tempCategoryId = Integer.parseInt(categoryId);
            } catch (NumberFormatException e) {
                System.err.println("Cảnh báo bảo mật: categoryId không phải là số. Mặc định về 0.");
                tempCategoryId = 0;
            }
        }
        final Integer finalCategoryId = tempCategoryId;

        // Lấy danh sách Categories để kiểm tra hợp lệ
        List<Category> categoryList = categoryService.getCategories();
        boolean isValidCategory = categoryList.stream().anyMatch(c -> c.getCateId() == finalCategoryId) || finalCategoryId == 0;

        Integer categoryIdForModel = finalCategoryId; // Biến này dùng để truyền về View

        if (keyword != null) {
            // TRƯỜNG HỢP A: ĐANG TÌM KIẾM THEO KEYWORD

            if (finalCategoryId > 0 && isValidCategory) {
                // Lọc theo Category VÀ Keyword
                // Lưu ý: Bạn cần một service mới hỗ trợ cả hai tham số này
                list = productService.searchNonActiveProductsByCategoryAndKeyword(finalCategoryId, keyword, pageable);

            } else {
                // Chỉ tìm kiếm theo Keyword (Category = All)
                list = productService.getNonActiveSearchProducts(keyword, pageable);
                categoryIdForModel = 0; // Đảm bảo filter category hiển thị 'All'
            }

        } else {
            // TRƯỜNG HỢP B: KHÔNG CÓ KEYWORD (Chỉ lọc Category hoặc All)

            if (finalCategoryId > 0 && isValidCategory) {
                // Chỉ lọc theo Category
                list = productService.getNonActiveProductsByCategory(finalCategoryId, pageable);
            } else {
                // Mặc định: Lấy tất cả sản phẩm
                list = productService.getAllNonActiveProductsPaged(pageable);
                categoryIdForModel = 0;
            }
        }


        // --- BƯỚC 3: XỬ LÝ PHÂN TRANG (CẦN ĐẶT SAU KHI LẤY LIST) ---
        if (list.getTotalPages() > 0 && page > list.getTotalPages()) {
            page = list.getTotalPages();
            pageable = PageRequest.of(page - 1, size);

            // Cần gọi lại service tương ứng với logic đã chọn
            // Cần tinh chỉnh logic này để tránh gọi lại nhiều lần và phức tạp
            // Dễ hơn là bạn chỉ cần kiểm tra page < 1 ở đầu và để Thymeleaf xử lý
            // Nhưng nếu muốn fix page ở đây:

            // (Bỏ qua việc gọi lại ở đây cho gọn, chỉ cần đảm bảo Thymeleaf xử lý số trang đúng)
        }


        // --- BƯỚC 4: TRUYỀN DỮ LIỆU VỀ VIEW ---
        model.addAttribute("pageProduct", list);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("selectedCategoryId", categoryIdForModel);
        model.addAttribute("keyword", keyword); // Truyền keyword về view


        return "product/deleted-list";
    }


    @GetMapping(value = "/edit/{proId}")
    public String showEditForm(@PathVariable("proId") String idStr, Model model) {

        Integer proId = null;

        try {
            proId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            // ID là chữ! Gán null hoặc -1 để đảm bảo productService trả về null
            System.err.println("ID nhập vào không phải là số: " + idStr);
            proId = -1; // Hoặc một giá trị chắc chắn không tồn tại
        }


        model.addAttribute("title", "Edit Product");
        model.addAttribute("product", productService.getProductById(proId));
        model.addAttribute("categoryList", categoryService.getCategories());

        return "product/edit";
    }


    // Ví dụ về ProductService
    // @Autowired
    // private ProductService productService;

    @PostMapping("/edit/{id}")
    public String updateProduct(@ModelAttribute("product") Product product, BindingResult bindingResult, @PathVariable("id") int proId, @RequestParam(value = "file", required = false) MultipartFile file, Model model) { // Tên "file" phải khớp với name="file" trong HTML

        boolean hasError = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isBarista = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_BARISTA"));

        // Kiểm tra vai trò Admin (có thể dùng isBarista || isAdmin)
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        // 1. KIỂM TRA MÂU THUẪN ID
        if (proId != product.getProId()) {
            System.err.println("SECURITY ALERT: Product ID mismatch. URL ID: " + proId + ", Form ID: " + product.getProId());

            // Trả về trang lỗi
            model.addAttribute("title", "Lỗi Bảo Mật Dữ Liệu");
            model.addAttribute("errorMessage", "Thông tin ID sản phẩm không nhất quán. Yêu cầu của bạn đã bị từ chối để đảm bảo an toàn hệ thống.");
            return "error-page"; // Trả về error-page.html
        }


        Product originalProduct = productService.getProductById(proId);
        if (isBarista && !isAdmin) {
            // 1. Chỉ cập nhật Status (và các trường liên quan đến trạng thái)
            originalProduct.setStatus(product.getStatus());
            productService.saveProduct(originalProduct);
            return "redirect:/home";
            // 2. Không cho phép Barista sửa các trường khác
            // (Đảm bảo các giá trị khác của product từ form KHÔNG được gán)
            // Validation Namejkjkj
        } else if (isAdmin) {

            String proName = product.getProName();
            if (proName == null || proName.trim().isEmpty()) {
                model.addAttribute("nameError", "Product name cant be empty");
                hasError = true;
            } else if (!proName.matches(VIETNAMESE_NAME_PATTERN)) {
                model.addAttribute("nameError", "Name just can contain letters!");
                hasError = true;
            }

            //Validation price
            if (bindingResult.hasFieldErrors("price")) {
                // Lỗi xảy ra khi Spring KHÔNG THỂ chuyển chuỗi từ form thành double (ví dụ: nhập chữ, bỏ trống)
                // Lấy thông báo lỗi cụ thể (thường là TypeMismatch)
                FieldError priceError = bindingResult.getFieldError("price");

                // Trường hợp lỗi binding là do nhập chữ hoặc bỏ trống.
                model.addAttribute("priceError", "Price must be a number");
                hasError = true;
            }

            if (bindingResult.hasFieldErrors("quantity")) {

                FieldError quantityError = bindingResult.getFieldError("quantity");

                model.addAttribute("quantityError", "Quantity must ve a number");
                hasError = true;
            }


            if (!hasError) {
                double priceValue = product.getPrice(); // Lấy giá trị double

                // Kiểm tra giá trị tối thiểu
                if (priceValue < 1000) {
                    // Lỗi này bao gồm cả trường hợp người dùng nhập 0 (vì 0 < 1000)
                    model.addAttribute("priceError", "Minimum price is 1000.");
                    hasError = true;
                }

                int quantityValue = product.getQuantity();
                if (quantityValue < 0) {
                    model.addAttribute("quantityError", "Quantity must be a non-negative number");

                }
            }


            //Validation Description

            String description = product.getDescription();
            if (!description.matches(VIETNAMESE_NAME_PATTERN) && description.trim().length() > 0) {
                model.addAttribute("descriptionError", "Description just can contain letters!");
                hasError = true;
            }


            // BƯỚC 1: Xử lý file ảnh mới (nếu người dùng có chọn)
            if (file != null && !file.isEmpty()) {

                // --- BẮT ĐẦU VALIDATION FILE ---

                // 1. Kiểm tra Kích thước file (ví dụ: Max 5MB)
                final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
                if (file.getSize() > MAX_FILE_SIZE) {
                    model.addAttribute("fileError", "File size is larger than 5MB");
                    hasError = true;
                }

                // 2. Kiểm tra Loại file (MIME Type)
                List<String> allowedContentTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");
                if (!allowedContentTypes.contains(file.getContentType())) {
                    model.addAttribute("fileError", "Not valid file type (JPEG, PNG, GIF).");
                    hasError = true;
                }

                // --- KẾT THÚC VALIDATION FILE ---

                // Chỉ tiếp tục lưu file nếu KHÔNG có lỗi validation (hasError vẫn là false)
                if (!hasError) {
                    try {
                        // 1. Tạo tên file mới và duy nhất (để tránh trùng lặp)
                        String originalFileName = file.getOriginalFilename();
                        // Lấy phần mở rộng (extension) của file, ví dụ: .jpg, .png
                        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                        // Tạo tên file mới bằng UUID
                        String newFileName = UUID.randomUUID().toString() + extension;

                        // 2. Định nghĩa đường dẫn file trên ổ đĩa
                        Path targetPath = Paths.get(UPLOAD_DIR, newFileName);

                        // 3. **Lưu file** vào thư mục bên ngoài project
                        file.transferTo(targetPath);

                        product.setImg(newFileName);

                    } catch (IOException e) {
                        // Xử lý lỗi khi lưu file (lỗi I/O, lỗi hệ thống)
                        model.addAttribute("fileError", " Error to save img");
                        hasError = true; // Đánh dấu lỗi hệ thống
                        e.printStackTrace();
                    }
                }
            }
            // ELSE: Nếu file.isEmpty() (người dùng không chọn file mới),
            // thì trường product.getImg() vẫn giữ lại tên file CŨ
            // nhờ vào input hidden trong form. KHÔNG CẦN làm gì thêm.

            if (hasError) {
                // Đảm bảo truyền lại danh sách category nếu có lỗi
                // model.addAttribute("categoryList", categoryService.findAll());
                model.addAttribute("categoryList", categoryService.getCategories());
                return "/product/edit";
            }

        }


        originalProduct.setProName(product.getProName());
        originalProduct.setPrice(product.getPrice());
        originalProduct.setDescription(product.getDescription());
        originalProduct.setCategory(product.getCategory());
        originalProduct.setActive(product.isActive());
        originalProduct.setImg(product.getImg());
        originalProduct.setCategory(product.getCategory());
        if (originalProduct.getQuantity() == 0) {
            originalProduct.setStatus("Unavailable");
        } else {
            originalProduct.setStatus(product.getStatus());
        }
        originalProduct.setQuantity(product.getQuantity());
        System.out.println("Product nay la: " + originalProduct.isActive());

        productService.saveProduct(originalProduct);

        return "redirect:/product/list";
    }

    @GetMapping(value = "/create")
    public String showCreateForm(Model model) {

        model.addAttribute("title", "Create Product");
        model.addAttribute("product", new Product());
        model.addAttribute("categoryList", categoryService.getCategories());

        return "product/create";
    }

    @PostMapping(value = "/create")
    public String createProduct(@ModelAttribute("product") Product product, BindingResult bindingResult, @RequestParam("file") MultipartFile file, Model model) { // Tên "file" phải khớp với name="file" trong HTML
        boolean hasError = false;
        product.setProId(0);
        // Validation Name
        String proName = product.getProName();
        if (proName == null || proName.trim().isEmpty()) {
            model.addAttribute("nameError", "Product name cant be empty");
            hasError = true;
        } else if (!proName.matches(VIETNAMESE_NAME_PATTERN)) {
            model.addAttribute("nameError", "Name just can contain letters!");
            hasError = true;
        }

        //Validation price
        if (bindingResult.hasFieldErrors("price")) {
            // Lỗi xảy ra khi Spring KHÔNG THỂ chuyển chuỗi từ form thành double (ví dụ: nhập chữ, bỏ trống)
            // Lấy thông báo lỗi cụ thể (thường là TypeMismatch)
            FieldError priceError = bindingResult.getFieldError("price");

            // Trường hợp lỗi binding là do nhập chữ hoặc bỏ trống.
            model.addAttribute("priceError", "Giá sản phẩm không hợp lệ. Vui lòng nhập một số.");
            hasError = true;
        }
        if (!hasError) {
            double priceValue = product.getPrice(); // Lấy giá trị double

            // Kiểm tra giá trị tối thiểu
            if (priceValue < 1000) {
                // Lỗi này bao gồm cả trường hợp người dùng nhập 0 (vì 0 < 1000)
                model.addAttribute("priceError", "Giá sản phẩm phải tối thiểu 1000.");
                hasError = true;
            }
        }


        //Validation Description

        String description = product.getDescription();
        if (!description.matches(VIETNAMESE_NAME_PATTERN) && description.trim().length() > 0) {
            model.addAttribute("descriptionError", "Description just can contain letters!");
            hasError = true;
        }


        // BƯỚC 1: Xử lý file ảnh mới (nếu người dùng có chọn)
        if (!file.isEmpty()) {

            // --- BẮT ĐẦU VALIDATION FILE ---

            // 1. Kiểm tra Kích thước file (ví dụ: Max 5MB)
            final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > MAX_FILE_SIZE) {
                model.addAttribute("fileError", "Kích thước file vượt quá giới hạn cho phép (5MB).");
                hasError = true;
            }

            // 2. Kiểm tra Loại file (MIME Type)
            List<String> allowedContentTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");
            if (!allowedContentTypes.contains(file.getContentType())) {
                model.addAttribute("fileError", "File không phải là định dạng hình ảnh hợp lệ (JPEG, PNG, GIF).");
                hasError = true;
            }

            // --- KẾT THÚC VALIDATION FILE ---

            // Chỉ tiếp tục lưu file nếu KHÔNG có lỗi validation (hasError vẫn là false)
            if (!hasError) {
                try {
                    // 1. Tạo tên file mới và duy nhất (để tránh trùng lặp)
                    String originalFileName = file.getOriginalFilename();
                    // Lấy phần mở rộng (extension) của file, ví dụ: .jpg, .png
                    String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    // Tạo tên file mới bằng UUID
                    String newFileName = UUID.randomUUID().toString() + extension;

                    // 2. Định nghĩa đường dẫn file trên ổ đĩa
                    Path targetPath = Paths.get(UPLOAD_DIR, newFileName);

                    // 3. **Lưu file** vào thư mục bên ngoài project
                    file.transferTo(targetPath);

                    product.setImg(newFileName);

                } catch (IOException e) {
                    // Xử lý lỗi khi lưu file (lỗi I/O, lỗi hệ thống)
                    model.addAttribute("fileError", "Lỗi hệ thống khi lưu file ảnh lên ổ đĩa.");
                    hasError = true; // Đánh dấu lỗi hệ thống
                    e.printStackTrace();
                }
            }
        }
        // ELSE: Nếu file.isEmpty() (người dùng không chọn file mới),
        // thì trường product.getImg() vẫn giữ lại tên file CŨ
        // nhờ vào input hidden trong form. KHÔNG CẦN làm gì thêm.


        // This catches TypeMismatch (e.g., user enters text for the ID) or conversion errors.
        // Nếu người dùng nhập chữ/ký tự lạ, lỗi vẫn sẽ nằm ở bindingResult.
        if (bindingResult.hasFieldErrors("category.cateId")) {
            model.addAttribute("categoryError", "ID danh mục không hợp lệ. Vui lòng chọn lại.");
            hasError = true;
        }

// 2. Kiểm tra nếu không có lỗi binding, nhưng giá trị ID không hợp lệ (<= 0).
// Lưu ý: Nếu CateId là int, giá trị mặc định là 0 nếu không được chọn.
// Chúng ta chỉ kiểm tra khi chưa có lỗi trước đó.
        if (!hasError) {
            // Tránh NullPointerException bằng cách kiểm tra product.getCategory() trước
            if (product.getCategory() == null || product.getCategory().getCateId() <= 0) {
                model.addAttribute("categoryError", "Vui lòng chọn một danh mục sản phẩm.");
                hasError = true;
            }
            // 3. Kiểm tra ID không tồn tại trong DB (Chỉ thực hiện khi có ID hợp lệ > 0)
            else {
                int submittedCateId = product.getCategory().getCateId();

                // Giả sử categoryService.getCategoryById trả về null nếu không tìm thấy.
                if (categoryService.getCategoryById(submittedCateId) == null) {
                    model.addAttribute("categoryError", "Danh mục đã chọn không tồn tại.");
                    hasError = true;
                }
            }
        }


        if (hasError) {
            // Đảm bảo truyền lại danh sách category nếu có lỗi
            // model.addAttribute("categoryList", categoryService.findAll());
            model.addAttribute("categoryList", categoryService.getCategories());
            return "/product/create";
        }


        // BƯỚC 2: Lưu Product (đã có tên ảnh mới hoặc cũ) vào Database
        product.setStatus("Available");
        product.setActive(true);
        productService.saveProduct(product);
        return "redirect:/product/list";
    }

    @PostMapping(value = "/delete/{id}")
    public String deleteProduct(@PathVariable("id") int proId) {

        productService.deleteSortProduct(productService.getProductById(proId));
        return "redirect:/product/list";
    }

    @GetMapping(value = "/{id}")
    public String showDetails(@PathVariable("id") String idStr, Model model) {

        model.addAttribute("title", "Product Details");
        Integer proId = null;

        try {
            proId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            // ID là chữ! Gán null hoặc -1 để đảm bảo productService trả về null
            System.err.println("ID nhập vào không phải là số: " + idStr);
            proId = -1; // Hoặc một giá trị chắc chắn không tồn tại
        }

        int cusId = 0;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            Customer customer = loggedUser.getLoggedCustomer();
            cusId = customer.getCusId();
            model.addAttribute("customerId", cusId);
        }

        List<Integer> bought = orderItemService.getProductIdsByCustomerId(cusId);

        boolean isBought = bought != null && bought.contains(proId);

        model.addAttribute("isBought", isBought);

        List<Feedback> feedbackList = feedbackService.getAllFeedback(proId);
        // Nếu proId là -1, productService.getProductById(-1) sẽ trả về null
        model.addAttribute("product", productService.getProductById(proId));

        model.addAttribute("feedbackList", feedbackList);

        // View sẽ xử lý product == null để hiển thị thông báo lỗi (như đã sửa ở câu trước)
        return "product/details";
    }

    @PostMapping("/{id}")
    public String addFeedback(@PathVariable("id") int productId,
                              @RequestParam("content") String content,
                              RedirectAttributes redirectAttributes) {

        Customer customer = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            customer = loggedUser.getLoggedCustomer();
        }

        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "The feedback content cannot be empty!");
            return "redirect:/product/" + productId;
        }

        Product product = productService.getProductById(productId);
        System.out.println(productId + " " + product.getProId());
        // Tạo đối tượng Feedback mới
        Feedback feedback = new Feedback();
        feedback.setCustomer(customer);
        feedback.setProduct(product);
        feedback.setContent(content);
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackService.saveFeedback(feedback);
        return "redirect:/product/" + productId;
    }

    @PostMapping("/{productId}/feedback/{fid}/edit")
    public String editFeedback(@PathVariable("productId") int productId,
                               @PathVariable("fid") int feedbackId,
                               @RequestParam("content") String newContent,
                               RedirectAttributes redirectAttributes) {

        if (newContent == null || newContent.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "The feedback content cannot be empty!");
            return "redirect:/product/" + productId;
        }
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);
        feedback.setContent(newContent);
        feedback.setCreatedAt(LocalDateTime.now()); // Cập nhật lại thời gian
        feedbackService.saveFeedback(feedback);

        return "redirect:/product/" + productId;
    }

    @PostMapping("/{productId}/feedback/{fid}/delete")
    public String deleteFeedback(@PathVariable("productId") int productId,
                                 @PathVariable("fid") int feedbackId) {

        feedbackService.deleteFeedback(feedbackId);
        return "redirect:/product/" + productId;
    }
}