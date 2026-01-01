package vn.edu.fpt.cafemanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.*;
import vn.edu.fpt.cafemanagement.repositories.OrderItemRepository;
import vn.edu.fpt.cafemanagement.repositories.OrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(int id) {
        return orderRepository.findById(id);
    }

    public void deleteOrder(int id) {
        orderRepository.deleteById(id);
    }

    public Page<Order> getPagedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return orderRepository.findAll(pageable);
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId);
    }

    // Lấy đơn đang hoạt động
    public Page<Order> getActiveOrders(Staff currentUser, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        String userRole = currentUser.getRole().getRoleName();

        return orderRepository.findActiveOrdersForUser(currentUser, userRole, pageable);
    }

    // Lấy đơn lịch sử (Served, Canceled)
    public Page<Order> getHistoryOrders(Staff currentUser, LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        String userRole = currentUser.getRole().getRoleName();

        return orderRepository.findCompletedOrdersByDateRange(currentUser, userRole, startDateTime, endDateTime, pageable);
    }

    public Page<Order> getUnservedOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<String> excludedStatuses = List.of("Served", "Canceled");
        return orderRepository.findByStatusNotIn(excludedStatuses, pageable);
    }

    /**
     * Lấy báo cáo sales và trả về dưới dạng Map.
     */
    public Map<String, Object> getSalesSummaryAsMap(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summaryMap = new HashMap<>();

        if (startDate == null || endDate == null) {
            return createEmptySummaryMap();
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();

        LocalDateTime adjustedEndDateTime = endDate.plusDays(1).atStartOfDay();

        List<Object[]> results = orderRepository.getSalesSummaryObject(startDateTime, adjustedEndDateTime);

        if (!results.isEmpty()) {
            Object[] row = results.get(0);

            double totalRevenue = ((Number) row[0]).doubleValue();
            long totalOrders = ((Number) row[1]).longValue();
            long vouchersUsed = ((Number) row[2]).longValue();
            double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalRevenue", totalRevenue);
            summary.put("totalOrders", totalOrders);
            summary.put("vouchersUsed", vouchersUsed);
            summary.put("averageOrderValue", averageOrderValue);
            return summary;
        }
        return Collections.emptyMap();
    }

    // Hàm tiện ích
    private Map<String, Object> createEmptySummaryMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalRevenue", 0.0);
        map.put("totalOrders", 0L);
        map.put("vouchersUsed", 0L);
        map.put("averageOrderValue", 0.0);
        return map;
    }

    public List<Order> getOrdersByPeriod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime adjustedEndDateTime = endDate.plusDays(1).atStartOfDay();
        return orderRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(startDateTime, adjustedEndDateTime);
    }

    public Page<Order> getServedOrCanceledOrders(Staff currentUser, LocalDate startDate, LocalDate endDate, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        String userRole = currentUser.getRole().getRoleName();

        return orderRepository.findCompletedOrdersByDateRange(currentUser, userRole, startDateTime, endDateTime, pageable);
    }

    public void updateOrder(Order order, Staff currentStaff) {
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public Page<Order> getKitchenOrders(Staff currentUser, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        String userRole = currentUser.getRole().getRoleName();

        return orderRepository.findActiveOrdersForUser(currentUser, userRole, pageable);
    }

    @Transactional
    public void deleteOrderById(int orderId) {
        orderItemRepository.deleteByOrder_OrderId(orderId);
        orderRepository.deleteById(orderId);
    }

    public Order findByTable(Table table) {
//        return orderRepository.findActiveOrder(table);
        return orderRepository.findFirstByTable(table);
    }

    public List<Integer> getUsedVoucherIdsByCustomer(Customer customer) {
        if (customer == null) {
            return Collections.emptyList();
        }
        return orderRepository.findUsedVoucherIdsByCustomerId(customer.getCusId());
    }

    public Page<Order> getOrderHistoryByCustomerId(int customerId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return orderRepository.findByCustomerCusIdOrderByCreatedAtDesc(customerId, pageable);
    }

    public Table findOccupiedTableByCustomer(Customer customer) {
        if (customer == null) {
            return null;
        }
        List<Order> orders = orderRepository.findOrderByCustomerAndOccupiedTable(customer);
        if (orders != null && !orders.isEmpty()) {
            // Tìm thấy, trả về bàn của đơn hàng mới nhất
            return orders.get(0).getTable();
        }
        return null;
    }
}