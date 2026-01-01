package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.TableBooking;
import vn.edu.fpt.cafemanagement.repositories.TableBookingRepository;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.OrderService;
import vn.edu.fpt.cafemanagement.services.TableService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/table")
public class TableController {
    private final LoggedUser loggedUser;
    private final TableService tableService;
    private final OrderService orderService;
    private final TableBookingRepository tableBookingRepository;

    public TableController(TableService tableService, LoggedUser loggedUser, OrderService orderService, TableBookingRepository tableBookingRepository) {
        this.tableService = tableService;
        this.loggedUser = loggedUser;
        this.orderService = orderService;
        this.tableBookingRepository = tableBookingRepository;
    }

    @GetMapping(path = "/list")
    public String showTableList(Model model) {
        List<Table> tables = tableService.getTablesList();
        model.addAttribute("tables", tables);
        return "table/table-list";
    }

    @GetMapping(path = "/management")
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER')")
    public String showTableListManagement(Model model,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) Integer capacity) {
        List<Table> tables = tableService.getTablesList();

        List<Integer> capacityList = tableService.getCapacityList();

        if (status != null && !status.isEmpty()) {
            tables = tables.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        if (capacity != null) {
            tables = tables.stream().filter(
                            t -> t.getCapacity() == capacity)
                    .collect(Collectors.toList());
        }
        model.addAttribute("status", status);
        model.addAttribute("capacity", capacity);
        model.addAttribute("tables", tables);
        model.addAttribute("available", tableService.getAvailableTables());
        model.addAttribute("occupied", tableService.getOccupiedTables());
        model.addAttribute("tables", tables);
        model.addAttribute("capacityList", capacityList);

        return "staff/table/table-list-staff";
    }

    @PostMapping(path = "/management/update-status")
    public String updateTableStatus(@RequestParam("status") String status, @RequestParam("tableId") int tableId) {
//        Table oldTable = tableService.findById(table.getTableId());
//
//        if("available".equalsIgnoreCase(oldTable.getStatus())) {
//            return "Update status error";
//        }

        tableService.updateTableStatus(tableId, status);
        return "redirect:/table/management?success=Update table status successfully";
    }

    @PostMapping("/management/move")
    @Transactional
    public String move(@RequestParam("oldTableId") int oldTableId,
                       @RequestParam("newTableId") int newTableId) {

        if (oldTableId == newTableId) {
            return "redirect:/table/management?error=Same Table";
        }

        Table oldTable = tableService.findById(oldTableId);
        Table newTable = tableService.findById(newTableId);

        if (oldTable == null || newTable == null) {
            return "redirect:/table/list?error=Old table or new table not found";
        }

        if (!"available".equalsIgnoreCase(newTable.getStatus())) {
            return "redirect:/table/management?error=New table is not available";
        }

        // Move table in table booking
        TableBooking tableBooking = tableBookingRepository.findFirstByTable(oldTable);
        if (tableBooking == null) {
            return "redirect:/table/management?error=Table booking not found";
        }

        tableBooking.setTable(newTable);
        tableBookingRepository.save(tableBooking);


        Order order = orderService.findByTable(oldTable);

        if (order == null) {
            return "redirect:/table/management?error=No order found";
        }

        // Update table status
        oldTable.setStatus("available");
        newTable.setStatus("occupied");

        // Move the order
        order.setTable(newTable);

        tableService.save(oldTable);
        tableService.save(newTable);
        orderService.saveOrder(order);

        return "redirect:/table/management?success=Move table successfully";
    }

}
