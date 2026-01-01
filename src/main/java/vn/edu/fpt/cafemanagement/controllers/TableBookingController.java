//package vn.edu.fpt.cafemanagement.controllers;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import vn.edu.fpt.cafemanagement.entities.Customer;
//import vn.edu.fpt.cafemanagement.entities.Table;
//import vn.edu.fpt.cafemanagement.entities.TableBooking;
//import vn.edu.fpt.cafemanagement.security.LoggedUser;
//import vn.edu.fpt.cafemanagement.services.CustomerService;
//import vn.edu.fpt.cafemanagement.services.TableBookingService;
//import vn.edu.fpt.cafemanagement.services.TableService;
//
//import java.time.Duration;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@Controller
//@RequestMapping(value = "/table/booking")
//public class TableBookingController {
//    private final LoggedUser loggedUser;
//    private final TableService tableService;
//    private final TableBookingService tableBookingService;
//    private final CustomerService customerService;
//
//    public TableBookingController(TableService tableService, LoggedUser loggedUser, TableBookingService tableBookingService, CustomerService customerService) {
//        this.tableService = tableService;
//        this.loggedUser = loggedUser;
//        this.tableBookingService = tableBookingService;
//        this.customerService = customerService;
//    }
//
//    @GetMapping(value = "/my")
//    public String showHistory(Model model,
//                              @RequestParam(value = "page", defaultValue = "1") int page,
//                              @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//                              @RequestParam(value = "status", required = false) String status) {
//        Customer loggedCustomer = loggedUser.getLoggedCustomer();
//
//        LocalDateTime startDateTime = null;
//        LocalDateTime endDateTime = null;
//        if (startDate != null) {
//            if(endDate != null){
//                startDateTime = startDate.atStartOfDay();
//                endDateTime = endDate.atTime(23, 59, 59);
//            } else {
//                endDateTime = LocalDateTime.now();
//            }
//
//        }
//
//
//        // Pagination
//        int size = 10;
//        if (page < 1) {
//            page = 1;
//        }
//
//        int pageIndex = Math.max(page - 1, 0);
//        Pageable pageable = PageRequest.of(pageIndex, size);
//
//        Page<TableBooking> tableBooking = tableBookingService.findTableBookingCustomer(loggedCustomer.getCusId(), status, startDateTime, endDateTime, pageable);
//
//        if (page > tableBooking.getTotalPages()) {
//            page = tableBooking.getTotalPages();
//            pageIndex = Math.max(page - 1, 0);
//            pageable = PageRequest.of(pageIndex, size);
//            tableBooking = tableBookingService.findTableBookingCustomer(loggedCustomer.getCusId(), status, startDateTime, endDateTime, pageable);
//        }
/// /        List<TableBooking> tableBooking = tableBookingService.findByCustomerId(loggedCustomer.getCusId());
//
//        List<String> bookingStatus = new ArrayList<>(Arrays.asList("booked", "canceled", "checked-in", "auto-canceled", "completed"));
//        int totalPages = Math.max(tableBooking.getTotalPages(), 1);
//
//
//        model.addAttribute("bookingStatus", bookingStatus);
//        model.addAttribute("tableBooking", tableBooking);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", totalPages);
//        model.addAttribute("startDate", startDateTime);
//        model.addAttribute("endDate", endDateTime);
//        model.addAttribute("status", status);
//        return "table-booking/view-table-booking";
//    }
//
//    @GetMapping(path = "/new")
//    public String showBookingTableForm(Model model, @RequestParam("table-id") int tableId) {
//
/// /        if (model.containsAttribute("errorMessage")) {
/// /            System.out.println("Có lỗi: " + model.getAttribute("errorMessage"));
/// /        }
//
//        Customer loggedCustomer = loggedUser.getLoggedCustomer();
//        Table table = tableService.findById(tableId);
//        TableBooking tableBooking = new TableBooking();
//        tableBooking.setTable(table);
//
//        tableBooking.setCustomer(loggedCustomer);
//
//        model.addAttribute("now", LocalDate.now());
//        model.addAttribute("tableBooking", tableBooking);
//        model.addAttribute("table", table);
//        model.addAttribute("loggedCustomer", loggedCustomer);
//
//        return "table-booking/create-booking";
//    }
//
//    @PostMapping(path = "/new")
//    public String addTableBooking(Model model, @ModelAttribute TableBooking tableBooking, RedirectAttributes redirectAttributes) {
//        Customer loggedCustomer = loggedUser.getLoggedCustomer();
//        tableBooking.setCustomer(loggedCustomer);
//
//        LocalDateTime bookingTime = tableBooking.getBookingTime();
//        LocalDateTime now = LocalDateTime.now();
//
//
//        try {
//            tableBookingService.createBooking(tableBooking);
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
//        }
//
//        tableService.updateTableStatus(tableBooking.getTable().getTableId(), "booked");
//
//        redirectAttributes.addFlashAttribute("successMessage", "Table booking has been saved successfully!");
//
//        return "redirect:/table/list?book-success";
//    }
//
//
//    @PostMapping(value = "/cancel")
//    public String cancelBooking(Model model, @RequestParam("tableBookingId") int bookingId, RedirectAttributes redirectAttributes) {
//        Customer loggedCustomer = loggedUser.getLoggedCustomer();
//
//        TableBooking tableBooking = tableBookingService.findById(bookingId);
//        if (tableBooking.getCustomer().getCusId() != loggedCustomer.getCusId()) {
//            return "redirect:/table/booking/my?cancel=failed";
//        }
//
////        tableService.updateTableStatus(tableBooking.getTable().getTableId(), "available");
//
//        tableBooking.setStatus("canceled");
//        tableBooking.getTable().setStatus("available");
//        tableBookingService.save(tableBooking);
//
//        return "redirect:/table/booking/my?cancel=success";
//    }
//
//    @GetMapping(value = "/management")
//    public String showBookingManagement(Model model,
//                                        @RequestParam(value = "page", defaultValue = "1") int page,
//                                        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//                                        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
//                                        @RequestParam(value = "status", required = false) String status) {
//
//        LocalDateTime startDateTime = null;
//        LocalDateTime endDateTime = null;
//        if (startDate != null && endDate != null) {
//            startDateTime = startDate.atStartOfDay();
//            endDateTime = endDate.atTime(23, 59, 59);
//        }
//
//
//        // Pagination
//        int size = 10;
//        if (page < 1) {
//            page = 1;
//        }
//
//        int pageIndex = Math.max(page - 1, 0);
//        Pageable pageable = PageRequest.of(pageIndex, size);
//
//        Page<TableBooking> tableBooking = tableBookingService.findTableBookingManager( status, startDateTime, endDateTime, pageable);
//
//        if (page > tableBooking.getTotalPages()) {
//            page = tableBooking.getTotalPages();
//            pageIndex = Math.max(page - 1, 0);
//            pageable = PageRequest.of(pageIndex, size);
//            tableBooking = tableBookingService.findTableBookingManager(status, startDateTime, endDateTime, pageable);
//        }
//
//        List<String> bookingStatus = new ArrayList<>(Arrays.asList("booked", "canceled", "checked-in", "auto-canceled", "completed"));
//
//        int totalPages = Math.max(tableBooking.getTotalPages(), 1);
//        System.out.println(totalPages);
//
//
//        model.addAttribute("bookingStatus", bookingStatus);
//        model.addAttribute("tableBooking", tableBooking);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", totalPages);
//        model.addAttribute("startDate", startDate);
//        model.addAttribute("endDate", endDate);
//        model.addAttribute("status", status);
//        return "staff/table-booking/staff-table-booking-history";
//    }
//
////    @PostMapping(value="/management/checkin")
////    public String checkInBookingManagement(Model model, @RequestParam("tableBookingId") int bookingId, RedirectAttributes redirectAttributes) {
////        TableBooking booking = tableBookingService.findById(bookingId);
////
////        if(booking.getStatus().equals("canceled")) {
////            return "redirect:/table/booking/management?update=failed";
////        }
////
////        booking.setStatus("checked-in");
////        booking.getTable().setStatus("occupied");
////
////        tableBookingService.save(booking);
////        return "redirect:/table/booking/management?update=success";
////    }
////
////    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER')")
////    @PostMapping(value = "management/cancel")
////    public String cancelBookingManagement(Model model, @RequestParam("tableBookingId") int bookingId, RedirectAttributes redirectAttributes) {
////        TableBooking tableBooking = tableBookingService.findById(bookingId);
////
////        tableService.updateTableStatus(tableBooking.getTable().getTableId(), "available");
////
////        tableBooking.setStatus("canceled");
////        tableBookingService.save(tableBooking);
////
////        return "redirect:/table/booking/management?cancel=success";
////    }
//
//
//    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER')")
//    @PostMapping("/management/updateStatus")
//    public String updateStatus(@RequestParam("tableBookingId") int bookingId,
//                               @RequestParam("action") String actionType, RedirectAttributes redirectAttributes) {
//        TableBooking booking = tableBookingService.findById(bookingId);
//
//        if (actionType.equals("cancel")) {
//            booking.setStatus("canceled");
//            tableService.updateTableStatus(booking.getTable().getTableId(), "available");
//            return "redirect:/table/booking/management?cancel=success";
//        } else if (actionType.equals("checkin")) {
//            if (!booking.getStatus().equals("canceled")) {
//                booking.setStatus("checked-in");
//                tableService.updateTableStatus(booking.getTable().getTableId(), "occupied");
//            }
//        }
//
//        tableBookingService.save(booking);
//        return "redirect:/table/booking/management?update=success";
//    }
//
//
//
//
//}
package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.CustomerService;
import vn.edu.fpt.cafemanagement.services.TableBookingService;
import vn.edu.fpt.cafemanagement.services.TableService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping(value = "/table/booking")
public class TableBookingController {
    private final LoggedUser loggedUser;
    private final TableService tableService;
    private final TableBookingService tableBookingService;
    private final CustomerService customerService;

    public TableBookingController(TableService tableService, LoggedUser loggedUser, TableBookingService tableBookingService, CustomerService customerService) {
        this.tableService = tableService;
        this.loggedUser = loggedUser;
        this.tableBookingService = tableBookingService;
        this.customerService = customerService;
    }

    @GetMapping(value = "/my")
    public String showHistory(Model model,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(value = "status", required = false) String status) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        LocalDateTime now = LocalDateTime.now();

        if (startDate != null) {
            if (endDate != null) {
                if (startDate.isAfter(endDate)) {
                    endDate = startDate;
                }
                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atTime(23, 59, 59);
            } else if (startDate.isAfter(now.toLocalDate())) {
                endDateTime = startDate.atTime(23, 59, 59);
                endDate = startDate;
            } else {
                endDateTime = LocalDateTime.now();
                endDate = endDateTime.toLocalDate();
            }
        }


        // Pagination
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<TableBooking> tableBooking = tableBookingService.findTableBookingCustomer(loggedCustomer.getCusId(), status, startDateTime, endDateTime, pageable);

        if (page > tableBooking.getTotalPages()) {
            page = tableBooking.getTotalPages();
            pageIndex = Math.max(page - 1, 0);
            pageable = PageRequest.of(pageIndex, size);
            tableBooking = tableBookingService.findTableBookingCustomer(loggedCustomer.getCusId(), status, startDateTime, endDateTime, pageable);
        }
//        List<TableBooking> tableBooking = tableBookingService.findByCustomerId(loggedCustomer.getCusId());

        List<String> bookingStatus = new ArrayList<>(Arrays.asList("booked", "canceled", "checked-in", "auto-canceled", "completed"));
        int totalPages = Math.max(tableBooking.getTotalPages(), 1);


        model.addAttribute("bookingStatus", bookingStatus);
        model.addAttribute("tableBooking", tableBooking);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        return "table-booking/view-table-booking";
    }

    @GetMapping(path = "/new")
    public String showBookingTableForm(Model model, @RequestParam("table-id") int tableId) {

        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        Table table = tableService.findById(tableId);
        TableBooking tableBooking = new TableBooking();
        tableBooking.setTable(table);

        tableBooking.setCustomer(loggedCustomer);

        model.addAttribute("now", LocalDate.now());
        model.addAttribute("tableBooking", tableBooking);
        model.addAttribute("table", table);
        model.addAttribute("loggedCustomer", loggedCustomer);

        return "table-booking/create-booking";
    }

    @PostMapping(path = "/new")
    public String addTableBooking(Model model, @ModelAttribute TableBooking tableBooking, RedirectAttributes redirectAttributes) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();
        tableBooking.setCustomer(loggedCustomer);

        LocalDateTime bookingTime = tableBooking.getBookingTime();
        LocalDateTime now = LocalDateTime.now();


        try {
            tableBookingService.createBooking(tableBooking);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/table/booking/new?table-id=" + tableBooking.getTable().getTableId();
        }

        tableService.updateTableStatus(tableBooking.getTable().getTableId(), "booked");

        redirectAttributes.addFlashAttribute("successMessage", "Table booking has been saved successfully!");

        return "redirect:/table/list?book-success";
    }


    @PostMapping(value = "/cancel")
    public String cancelBooking(Model model, @RequestParam("tableBookingId") int bookingId, RedirectAttributes redirectAttributes) {
        Customer loggedCustomer = loggedUser.getLoggedCustomer();

        TableBooking tableBooking = tableBookingService.findById(bookingId);
        if (tableBooking.getCustomer().getCusId() != loggedCustomer.getCusId()) {
            return "redirect:/table/booking/my?cancel=failed";
        }

//        tableService.updateTableStatus(tableBooking.getTable().getTableId(), "available");

        tableBooking.setStatus("canceled");
        tableBooking.getTable().setStatus("available");
        tableBookingService.save(tableBooking);

        return "redirect:/table/booking/my?cancel=success";
    }

    @GetMapping(value = "/management")
    public String showBookingManagement(Model model,
                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                        @RequestParam(value = "status", required = false) String status) {

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        LocalDateTime now = LocalDateTime.now();


        if (startDate != null) {
            if (endDate != null) {
                if (startDate.isAfter(endDate)) {
                    endDate = startDate;
                }
                startDateTime = startDate.atStartOfDay();
                endDateTime = endDate.atTime(23, 59, 59);
            } else if (startDate.isAfter(now.toLocalDate())) {
                endDateTime = startDate.atTime(23, 59, 59);
                endDate = startDate;
            } else {
                endDateTime = LocalDateTime.now();
                endDate = endDateTime.toLocalDate();
            }
        }


        // Pagination
        int size = 10;
        if (page < 1) {
            page = 1;
        }

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<TableBooking> tableBooking = tableBookingService.findTableBookingManager(status, startDateTime, endDateTime, pageable);

        if (page > tableBooking.getTotalPages()) {
            page = tableBooking.getTotalPages();
            pageIndex = Math.max(page - 1, 0);
            pageable = PageRequest.of(pageIndex, size);
            tableBooking = tableBookingService.findTableBookingManager(status, startDateTime, endDateTime, pageable);
        }

        List<String> bookingStatus = new ArrayList<>(Arrays.asList("booked", "canceled", "checked-in", "auto-canceled", "completed"));

        int totalPages = Math.max(tableBooking.getTotalPages(), 1);
        System.out.println(totalPages);


        model.addAttribute("bookingStatus", bookingStatus);
        model.addAttribute("tableBooking", tableBooking);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        return "staff/table-booking/staff-table-booking-history";
    }


    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER')")
    @PostMapping("/management/update-status")
    public String updateStatus(@RequestParam("tableBookingId") int bookingId,
                               @RequestParam("action") String actionType, RedirectAttributes redirectAttributes) {
        TableBooking booking = tableBookingService.findById(bookingId);

        try {
            switch (actionType) {
                case "cancel" -> {
                    booking.setStatus("canceled");
                    tableService.updateTableStatus(booking.getTable().getTableId(), "available");
//                return "redirect:/table/booking/management?cancel=success";
                }
                case "checkin" -> {
                    if (!booking.getStatus().equals("canceled")) {
                        booking.setStatus("checked-in");
//                    tableService.updateTableStatus(booking.getTable().getTableId(), "occupied");
                    }
                }

                case "reject" -> {
                    booking.setStatus("rejected");
                    tableService.updateTableStatus(booking.getTable().getTableId(), "available");
                }
                case "confirm" -> booking.setStatus("booked");
            }
        } catch (Exception e) {
            return "redirect:/table/booking/management?error=Connect to server failed";
        }


        tableBookingService.save(booking);
        return "redirect:/table/booking/management?success=Update success";
    }
}
