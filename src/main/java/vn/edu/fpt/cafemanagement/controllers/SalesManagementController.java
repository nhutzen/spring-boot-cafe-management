package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import vn.edu.fpt.cafemanagement.services.ExcelExportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.repositories.OrderRepository;
import vn.edu.fpt.cafemanagement.services.OrderService;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class SalesManagementController {
    private final OrderService orderService;
    private final ExcelExportService excelExportService;

    public SalesManagementController(OrderService orderService, ExcelExportService excelExportService) {
        this.orderService = orderService;
        this.excelExportService = excelExportService;
    }

    // cái này tạm dùng để chuyển hướng sang dashboard thôi, mốt là làm trong RoleController
    @GetMapping
    public String index(Model model) {
        return "dashboard/index";
    }

    @GetMapping("/sales")
    public String showSalesPage(Model model) {
        model.addAttribute("orders", Collections.emptyList());
        return "dashboard/sales/sales";
    }

    @PostMapping("/sales")
    public String filterSalesReport(Model model,
                                    @RequestParam(name = "startDate") // required = true là mặc định
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                    @RequestParam(name = "endDate")
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (endDate.isBefore(startDate)) {
            model.addAttribute("errorMessage", "Ngày kết thúc phải sau ngày bắt đầu.");
            model.addAttribute("orders", Collections.emptyList());
        } else {
            Map<String, Object> summary = orderService.getSalesSummaryAsMap(startDate, endDate);
            List<Order> orderList = orderService.getOrdersByPeriod(startDate, endDate);
            List<Order> activeOrders = orderList.stream()
                    .filter(order -> !"Canceled".equalsIgnoreCase(order.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("salesSummary", summary);
            model.addAttribute("orders", activeOrders);
        }
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "dashboard/sales/sales";
    }

    @GetMapping("/sales/export")
    public void exportSalesToExcel(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   HttpServletResponse response) {
        try {
            Map<String, Object> summary = orderService.getSalesSummaryAsMap(startDate, endDate);
            List<Order> orders = orderService.getOrdersByPeriod(startDate, endDate);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String formattedDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Sales_Report_" + formattedDate + ".xlsx";
            response.setHeader(headerKey, headerValue);
            excelExportService.generateSalesReport(response, startDate, endDate, summary, orders);

        } catch (IOException e) {
            System.err.println("Error during Excel export: " + e.getMessage());
        }
    }
}