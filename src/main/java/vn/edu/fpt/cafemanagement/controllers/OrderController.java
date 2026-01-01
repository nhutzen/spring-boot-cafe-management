package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.fpt.cafemanagement.entities.*;
import vn.edu.fpt.cafemanagement.repositories.TableBookingRepository;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final ProductService productService;
    private final OrderService orderService;
    private final VoucherService voucherService;
    private final CustomerService customerService;
    private final CategoryService categoryService;
    private final PointHistoryService pointHistoryService;
    private final TableService tableService;
    private final TableBookingService tableBookingService;
    private final LoggedUser loggedUser;
    private final TableBookingRepository tableBookingRepository;

    public OrderController(ProductService productService,
                           OrderService orderService,
                           VoucherService voucherService,
                           CustomerService customerService,
                           CategoryService categoryService,
                           PointHistoryService pointHistoryService,
                           TableService tableService,
                           TableBookingService tableBookingService,
                           LoggedUser loggedUser,
                           TableBookingRepository tableBookingRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.voucherService = voucherService;
        this.customerService = customerService;
        this.categoryService = categoryService;
        this.pointHistoryService = pointHistoryService;
        this.tableService = tableService;
        this.tableBookingService = tableBookingService;
        this.loggedUser = loggedUser;
        this.tableBookingRepository = tableBookingRepository;
    }

    // ----------------------- [GET: Hiển thị form tạo order + tìm kiếm + phân trang] -----------------------
    @GetMapping("/create")
    public String showCreateOrderForm(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "customerPhone", required = false) String customerPhone,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model, HttpServletRequest request) {

        int pageSize = 15;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Product> productPage;

        Table bookedTable = null;
        List<Table> bookedTables = null;
        Customer customer = null;

        if ("check".equals(action)) {
            customer = customerService.getCustomerByPhone(customerPhone);
            if (customer == null) {
                model.addAttribute("error", "Customer not found!");
                model.addAttribute("customer", null);
            } else {
                model.addAttribute("customer", customer);

                // Ưu tiên 1: Kiểm tra ĐẶT BÀN (Booking)
                TableBooking activeBooking = tableBookingService.findActiveBookingByCustomer(customer); // (Hàm này nên tìm status "booked")

                if (activeBooking != null) {
                    bookedTable = activeBooking.getTable();
                } else {
                    // Ưu tiên 2: Kiểm tra ĐƠN HÀNG (Order) đang hoạt động
                    Table occupiedTable = orderService.findOccupiedTableByCustomer(customer);

                    if (occupiedTable != null) {
                        bookedTable = occupiedTable;
                    }
                }
            }
            model.addAttribute("customerPhone", customerPhone);
        } else {
            model.addAttribute("customer", null);
            model.addAttribute("customerPhone", null);
        }

        // --- TÌM KIẾM SẢN PHẨM ---
        if (query != null && !query.trim().isEmpty() && categoryId != null && categoryId > 0) {
            // tìm theo cả tên + category
            productPage = productService.searchActiveProductsByCategory(categoryId, query.trim(), pageable);
        } else if (query != null && !query.trim().isEmpty()) {
            // chỉ tìm theo tên
            productPage = productService.searchActiveProducts1(query.trim(), pageable);
        } else if (categoryId != null && categoryId > 0) {
            // chỉ lọc theo category
            productPage = productService.getProductsByCategoryPaged1(categoryId, pageable);
        } else {
            // Mặc định: lấy tất cả
            productPage = productService.getActiveProductsPaged1(pageable);
        }

        int totalPages = productPage.getTotalPages();
        if (page > totalPages && totalPages > 0) {
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                return "error/404 :: content";
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
            }
        }

        model.addAttribute("categoryList", categoryService.getCategories());
        model.addAttribute("selectedCategoryId", categoryId != null ? categoryId : 0);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("productList", productPage.getContent());

        if (customer != null) {
            // 1. Lấy tất cả voucher (đã lọc theo SL/Ngày hết hạn từ VoucherService)
            List<Voucher> allApplicableVouchers = voucherService.getActiveVouchers();

            // 2. Lấy danh sách voucher đã dùng
            List<Integer> usedVoucherIds = orderService.getUsedVoucherIdsByCustomer(customer);

            // 3. Lọc danh sách voucher
            if (usedVoucherIds != null && !usedVoucherIds.isEmpty()) {
                List<Voucher> availableVouchers = allApplicableVouchers.stream()
                        .filter(voucher -> !usedVoucherIds.contains(voucher.getVoucherId()))
                        .collect(Collectors.toList());
                model.addAttribute("voucherList", availableVouchers);
            } else {
                // Customer này chưa dùng voucher nào
                model.addAttribute("voucherList", allApplicableVouchers);
            }
        } else {
            // GUEST Không cho xem voucher
            model.addAttribute("voucherList", Collections.emptyList());
        }


        // Hiển thị dropdown bàn hay bàn cố định
        if (bookedTable != null) {
            // Nếu khách đã check-in, chỉ gửi thông tin bàn đó
            model.addAttribute("bookedTable", bookedTable);
            model.addAttribute("tableList", null); // Không cần danh sách bàn
        } else {
            // Nếu không, gửi danh sách các bàn 'available'
            model.addAttribute("bookedTable", null);
            List<Table> availableTables = tableService.getTablesList().stream()
                    .filter(table -> "available".equalsIgnoreCase(table.getStatus()) || "booked".equalsIgnoreCase(table.getStatus()))
                    .toList();
            model.addAttribute("tableList", availableTables);
        }

        model.addAttribute("customerPhone", customerPhone);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("title", "Create In-Store Order");

        return "order/create";
    }

    // ----------------------- [POST: Tạo order mới] -----------------------
    @Transactional
    @PostMapping("/create")
    public String createOrder(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "productIds", required = false) List<Integer> productIds,
            @RequestParam(value = "quantities", required = false) List<Integer> quantities,
            @RequestParam(value = "notes", required = false) List<String> notes,
            @RequestParam(value = "voucherId", required = false) Optional<Integer> voucherId,
            @RequestParam(value = "tableId", required = false) Integer tableId,
            @RequestParam(value = "customerPhone", required = false) String customerPhone,
            @RequestParam(value = "pointsUsed", defaultValue = "0") int pointsUsed,
            Model model
    ) {

        // --- [CHECK 1] Products ---
        if (productIds == null || productIds.isEmpty() || "null".equals(String.valueOf(productIds.get(0)))) {
            model.addAttribute("error", "No products selected!");
            return reloadCreatePage(model, null);
        }

        // --- [CHECK 2] Quantities ---
        if (quantities == null || quantities.size() != productIds.size()) {
            model.addAttribute("error", "Data mismatch between products and quantities. Please refresh and try again.");
            return reloadCreatePage(model, null);
        }

        // --- [CHECK 3] Get Customer ---
        Customer customer = null;
        if (customerPhone != null && !customerPhone.trim().isEmpty()) {
            customer = customerService.getCustomerByPhone(customerPhone.trim());
            if (customer == null) {
                model.addAttribute("warning", "Customer not found! Order will not be linked to any customer.");
            }
        }

        // --- [CHECK 4] Cannot use both ---
        Integer voucherIdValue = voucherId.orElse(0);
        if (pointsUsed > 0 && voucherIdValue != 0) {
            model.addAttribute("error", "Cannot use both Voucher and Redeem Points!");
            return reloadCreatePage(model, customer);
        }

        // --- CHECK 4.1: Guest không được dùng voucher ---
        if (voucherIdValue != 0 && customer == null) {
            model.addAttribute("error", "Vouchers are only applicable for registered customers. Please check phone number.");
            return reloadCreatePage(model, null); // customer ở đây là null
        }

        // --- [CHECK 5] Staff check ---
        Staff staff = loggedUser.getLoggedManager();
        if (staff == null) {
            model.addAttribute("error", "No logged-in staff! Please login again.");
            return "redirect:/login";
        }

        // --- [STEP 6] Lấy Voucher ---
        Voucher voucher = null;
        if (voucherIdValue != 0) {
            voucher = voucherService.getVoucherById(voucherIdValue);

            // Check 6: Voucher không tồn tại hoặc hết số lượng (từ code cũ)
            if (voucher == null || voucher.getQuantity() <= 0) {
                model.addAttribute("error", "Voucher invalid or out of stock!");
                return reloadCreatePage(model, customer);
            }

            // Check 6.1: Voucher hết hạn
            if (voucher.getEndDate() == null || voucher.getEndDate().isBefore(LocalDate.now())) {
                model.addAttribute("error", "This voucher is expired!");
                return reloadCreatePage(model, customer);
            }

            // Check 6.2: Khách hàng đã sử dụng voucher này
            // (customer != null đã được đảm bảo bởi CHECK 4.1)
            List<Integer> usedVoucherIds = orderService.getUsedVoucherIdsByCustomer(customer);
            if (usedVoucherIds != null && usedVoucherIds.contains(voucher.getVoucherId())) {
                model.addAttribute("error", "You have already used this voucher on a previous order.");
                return reloadCreatePage(model, customer);
            }
        }

        // --- [STEP 7] Tính Subtotal ---
        double totalPrice = 0; // Subtotal
        List<OrderItem> orderItems = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {
            Product product = productService.getProductById(productIds.get(i));

            if (product == null) {
                model.addAttribute("error", "One of the selected products is invalid or no longer exists!");
                return reloadCreatePage(model, customer);
            }

            int qty = quantities.get(i);

            // 1. Kiểm tra xem tồn kho có đủ không
            if (product.getQuantity() < qty) {
                model.addAttribute("error", "Not enough stock for '" + product.getProName() + "'. Only " + product.getQuantity() + " left.");
                return reloadCreatePage(model, customer);
            }

            // 2. Trừ số lượng tồn kho
            product.setQuantity(product.getQuantity() - qty);

            if (product.getQuantity() <= 0) {
                product.setStatus("Unavailable");
            }

            // 3. Lưu lại sản phẩm với số lượng mới
            productService.saveProduct(product);


            String note = (notes != null && notes.size() > i) ? notes.get(i) : "";

            OrderItem item = new OrderItem();
            item.setUnitPrice(product.getPrice());
            item.setProduct(product);
            item.setQuantity(qty);
            item.setNote(note);
            orderItems.add(item);
            totalPrice += item.getUnitPrice() * qty;
        }

        // --- [STEP 8] Tính Voucher Discount ---
        double voucherDiscount = 0;
        if (voucher != null) {
            if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = Math.ceil(totalPrice * (voucher.getDiscountValue() / 100.0));
            } else if ("AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
                voucherDiscount = voucher.getDiscountValue();
            }
        }

        // --- [STEP 9] Tính Price To Pay ---
        double priceToPay = totalPrice - voucherDiscount;
        if (priceToPay < 0) priceToPay = 0;

        // --- [STEP 10] Tính Max Points Needed ---
        int maxPointsNeeded = (int) Math.ceil(priceToPay / 1000.0);

        // --- [STEP 11 - VALIDATION] BẮT LỖI pointsUsed TẠI BACKEND ---
        if (pointsUsed < 0) {
            model.addAttribute("error", "Points used cannot be negative.");
            return reloadCreatePage(model, customer);
        }
        if (customer != null && pointsUsed > customer.getPoint()) {
            model.addAttribute("error", "You only have " + customer.getPoint() + " points. Please enter a lower amount.");
            return reloadCreatePage(model, customer);
        }
        if (pointsUsed > maxPointsNeeded) {
            model.addAttribute("error", "This order only requires a maximum of " + maxPointsNeeded + " points. Please enter a lower amount.");
            return reloadCreatePage(model, customer);
        }

        int actualPointsUsed = pointsUsed;

        // --- [STEP 12] TÍNH TOÁN CUỐI CÙNG ---
        double pointsDiscount = actualPointsUsed * 1000.0;
        double finalPrice = priceToPay - pointsDiscount;
        if (finalPrice < 0) finalPrice = 0;

        int earnedPoints = (int) (finalPrice / 50000);

        if (finalPrice > 0) {
            finalPrice = Math.ceil(finalPrice / 1000) * 1000;
        }

        // --- [STEP 13] LƯU VÀO DATABASE ---

        Order order = new Order();
        if (customer != null) order.setCustomer(customer);

        order.setStaff(staff);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus("Pending");
        order.setPointsUsed(actualPointsUsed);
        order.setTotalPrice(finalPrice);

        // --- GÁN BÀN VÀO ĐƠN HÀNG ---
        if (tableId != null && tableId > 0) {
            Table table = tableService.findById(tableId);
            TableBooking tableBooking = tableBookingRepository.findFirstByTableAndStatus(table, "booked");


            if (table != null) {
                // 1. Gán bàn vào đơn hàng
                order.setTable(table);

                // 2. Cập nhật trạng thái bàn thành "occupied"
                tableService.updateTableStatus(tableId, "occupied");

                if (tableBooking != null){
                   tableBooking.setStatus("checked-in");
                   tableBookingRepository.save(tableBooking);
                }

            } else {
                // Báo lỗi nếu ID bàn không hợp lệ
                model.addAttribute("warning", "Invalid Table ID. Order created as 'Take-away'.");
            }
        }

        // Gắn order vào items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);

        // Gắn voucher và giảm số lượng (nếu có)
        if (voucher != null) {
            order.setVoucher(voucher);
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherService.saveVoucher(voucher);
        }

        orderService.saveOrder(order); // Lưu order (và items, và table)

        // Cập nhật customer (nếu có)
        if (customer != null) {
            customer.setPoint(customer.getPoint() - actualPointsUsed + earnedPoints);
            customerService.saveCustomer(customer);

            // Ghi lịch sử
            if (actualPointsUsed > 0) {
                PointHistory ph = new PointHistory();
                ph.setCustomer(customer);
                ph.setOrder(order);
                ph.setAmount(-actualPointsUsed);
                ph.setTypeOfChange("Redeemed in order");
                ph.setChangeTime(LocalDateTime.now());
                pointHistoryService.saveHistory(ph);
            }
            if (earnedPoints > 0) {
                PointHistory phEarned = new PointHistory();
                phEarned.setCustomer(customer);
                phEarned.setOrder(order);
                phEarned.setAmount(earnedPoints);
                phEarned.setTypeOfChange("Earned from order");
                phEarned.setChangeTime(LocalDateTime.now());
                pointHistoryService.saveHistory(phEarned);
            }
        }

        model.addAttribute("success", "Order created successfully!");
        return reloadCreatePage(model, customer);
    }

    // ----------------------- [GET: Danh sách đơn hàng] -----------------------
    @GetMapping("/list")
    public String viewOrders(@RequestParam(defaultValue = "1") int page, Model model) {

        Staff currentUser = loggedUser.getLoggedManager();
        if (currentUser == null) {
            return "redirect:/login";
        }

        int pageSize = 6;
        Page<Order> orderPage = orderService.getActiveOrders(currentUser, page, pageSize);

        orderPage.getContent().forEach(order -> {
            double roundedPrice = Math.ceil(order.getTotalPrice() / 1000) * 1000;
            order.setTotalPrice(roundedPrice);
        });

        int totalPages = orderPage.getTotalPages();
        if (page > totalPages && totalPages > 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("customerList", customerService.getAllCustomers());
        return "order/list";
    }

    @GetMapping("/history-list")
    public String viewOrdersHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Staff currentUser = loggedUser.getLoggedManager();
        if (currentUser == null) {
            return "redirect:/login";
        }

        int pageSize = 6;
        Page<Order> orderPage = orderService.getHistoryOrders(currentUser, startDate, endDate, page, pageSize);

        orderPage.getContent().forEach(order -> {
            double roundedPrice = Math.ceil(order.getTotalPrice() / 1000) * 1000;
            order.setTotalPrice(roundedPrice);
        });

        int totalPages = orderPage.getTotalPages();
        if (page > totalPages && totalPages > 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "order/history-list";
    }

    @GetMapping("/edit")
    public String showEditOrderPage(
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        Staff currentUser = loggedUser.getLoggedManager();
        if (currentUser == null) {
            return "redirect:/login";
        }

        int pageSize = 6;
        Page<Order> orderPage = orderService.getKitchenOrders(currentUser, page, pageSize);

        int totalPages = orderPage.getTotalPages();
        if (page > totalPages && totalPages > 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("title", "Edit Order");

        return "order/edit";
    }

    @GetMapping("/edit-history")
    public String showEditOrderHistoryPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        Staff currentUser = loggedUser.getLoggedManager();
        if (currentUser == null) {
            return "redirect:/login";
        }

        int pageSize = 6;
        Page<Order> orderPage = orderService.getServedOrCanceledOrders(currentUser, startDate, endDate, page, pageSize);

        int totalPages = orderPage.getTotalPages();
        if (page > totalPages && totalPages > 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("title", "Edit Order History");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "order/edit-history";
    }

    // ----------------------- [Post: Sửa trạng thái đơn hàng] -----------------------
    @PostMapping("/updateStatus")
    @Transactional
    public String updateOrderStatus(
            @RequestParam("orderId") int orderId,
            @RequestParam("status") String status
    ) {

        // 1. Kiểm tra status rỗng (nếu Barista quên chọn)
        if (status == null || status.trim().isEmpty()) {
            return "redirect:/order/edit";
        }

        Staff currentUser = loggedUser.getLoggedManager();
        if (currentUser == null) {
            return "redirect:/login";
        }

        String userRoleName = currentUser.getRole().getRoleName();

        // 2. Xử lý "Canceled"
        if ("Canceled".equals(status)) {

            if (!userRoleName.equals("Barista")) {
                return "redirect:/order/edit?error=UnauthorizedCancel";
            }

            try {
                Optional<Order> optionalOrder = orderService.getOrderById(orderId);
                if (optionalOrder.isPresent()) {
                    Order order = optionalOrder.get();
                    order.setMadeBy(currentUser);
                    order.setStatus("Canceled");
                    orderService.updateOrder(order, currentUser);
                }


            } catch (Exception e) {
                return "redirect:/order/edit?error=UpdateFailed";
            }
            return "redirect:/order/edit?success=OrderCanceled";
        }

        // 3. Xử lý "Ready" và "Served"
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            return "redirect:/order/edit?error=OrderNotFound";
        }

        Order order = optionalOrder.get();

        if (status.equals("Ready") && userRoleName.equals("Barista")) {
            order.setMadeBy(currentUser); // Gán Barista
        } else if (status.equals("Ready")) {
            return "redirect:/order/edit?error=UnauthorizedReady";
        }

        if (status.equals("Served") && userRoleName.equals("Waiter")) {
            order.setServedBy(currentUser); // Gán Waiter
        } else if (status.equals("Served")) {
            return "redirect:/order/edit?error=UnauthorizedServed";
        }

        order.setStatus(status);

        // Service sẽ lưu order (với madeBy/servedBy đã được set)
        // và cũng sẽ cập nhật updatedAt, updatedBy (nếu có)
        orderService.updateOrder(order, currentUser);

        return "redirect:/order/edit";
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@PathVariable("id") int orderId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if (optionalOrder.isEmpty()) {
            response.put("success", false);
            response.put("message", "Order not found!");
            return response;
        }

        Order order = optionalOrder.get();
        List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);

        // Lấy customer và table (có thể null)
        Customer customer = order.getCustomer();
        Table table = order.getTable(); // Lấy thông tin bàn

        double roundedPrice = Math.ceil(order.getTotalPrice() / 1000) * 1000;

        Map<String, Object> orderMap = new HashMap<>();

        orderMap.put("id", order.getOrderId());
        orderMap.put("customer", customer != null ? customer.getName() : "N/A");
        orderMap.put("staff", order.getStaff() != null ? order.getStaff().getName() : "N/A");
        orderMap.put("status", order.getStatus());
        orderMap.put("table", table != null ? table.getTableId() : "Take-away");
        orderMap.put("pointsUsed", order.getPointsUsed());
        orderMap.put("voucher", order.getVoucher() != null ? order.getVoucher().getVoucherName() : "None");
        orderMap.put("totalPrice", roundedPrice);
        orderMap.put("date", order.getCreatedAt());

        LocalDateTime completedAt = (order.getUpdatedAt() != null &&
                (order.getStatus().equals("Served") || order.getStatus().equals("Canceled")))
                ? order.getUpdatedAt() : null;
        orderMap.put("update", completedAt);

        orderMap.put("products", items.stream()
                .map(i -> Map.of(
                        "name", i.getProduct().getProName(),
                        "price", i.getUnitPrice(),
                        "quantity", i.getQuantity()
                ))
                .toList());

        response.put("order", orderMap);
        response.put("success", true);

        if (customer != null) {
            Map<String, Object> customerMap = new HashMap<>();
            customerMap.put("id", customer.getCusId());
            customerMap.put("name", customer.getName());
            customerMap.put("phone", customer.getPhoneNumber());
            customerMap.put("email", customer.getEmail());
            customerMap.put("address", customer.getAddress());
            customerMap.put("img", customer.getImg());

            response.put("customer", customerMap);

        } else {
            response.put("customer", Map.of(
                    "id", "N/A",
                    "name", "N/A",
                    "phone", "N/A",
                    "email", "N/A",
                    "address", "",
                    "img", ""
            ));
        }

        return response;
    }


    private String reloadCreatePage(Model model, Customer customer) {
        model.addAttribute("categoryList", categoryService.getCategories());

        Page<Product> productPage = productService.getActiveProductsPaged(PageRequest.of(0, 10));
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", productPage.getTotalPages());

        if (customer != null) {
            List<Voucher> allApplicableVouchers = voucherService.getActiveVouchers();
            List<Integer> usedVoucherIds = orderService.getUsedVoucherIdsByCustomer(customer);
            if (usedVoucherIds != null && !usedVoucherIds.isEmpty()) {
                List<Voucher> availableVouchers = allApplicableVouchers.stream()
                        .filter(voucher -> !usedVoucherIds.contains(voucher.getVoucherId()))
                        .collect(Collectors.toList());
                model.addAttribute("voucherList", availableVouchers);
            } else {
                model.addAttribute("voucherList", allApplicableVouchers);
            }
        } else {
            // GUEST Không cho xem voucher
            model.addAttribute("voucherList", Collections.emptyList());
        }

        // Giữ lại customer (dù là null hay không)
        model.addAttribute("customer", customer);

        // Giữ lại SĐT nếu có
        if (customer != null) {
            model.addAttribute("customerPhone", customer.getPhoneNumber());
        } else {
            model.addAttribute("customerPhone", null);
        }

        model.addAttribute("selectedCategoryId", 0);
        model.addAttribute("query", "");
        model.addAttribute("title", "Create In-Store Order");

        List<Table> availableTables = tableService.getTablesList().stream()
                .filter(table -> "available".equalsIgnoreCase(table.getStatus()))
                .toList();
        model.addAttribute("tableList", availableTables);
        model.addAttribute("bookedTable", null); // Reset bookedTable khi reload

        return "order/create";
    }
}