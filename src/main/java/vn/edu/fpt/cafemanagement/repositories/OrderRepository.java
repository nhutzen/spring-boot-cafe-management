package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.entities.Staff;
import vn.edu.fpt.cafemanagement.entities.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByCustomerCusId(int cusId);

    // Chỉ lấy đơn đang hoạt động (Pending)
    @Query("SELECT o FROM Order o WHERE LOWER(o.status) IN ('Paid')")
    Page<Order> findActiveOrders(Pageable pageable);

    // Lấy đơn lịch sử (Served, Canceled)
    @Query("SELECT o FROM Order o WHERE LOWER(o.status) IN ('Served', 'Canceled')")
    Page<Order> findHistoryOrders(Pageable pageable);

    Page<Order> findByStatusIn(List<String> statuses, Pageable pageable);

    Page<Order> findByStatusNotIn(List<String> statuses, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0), COUNT(o), COUNT(o.voucher) " +
            "FROM Order o " +
            "WHERE o.createdAt >= :startDate AND o.createdAt < :endDate " +
            "AND o.status != 'Canceled'")
        // <-- Đã thêm điều kiện này
    List<Object[]> getSalesSummaryObject(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Order> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime startDate, LocalDateTime endDate);

    Optional<Order> findOrderByTable(Table table);

    @Query("SELECT DISTINCT o.voucher.voucherId FROM Order o WHERE o.customer.cusId = :customerId AND o.voucher IS NOT NULL")
    List<Integer> findUsedVoucherIdsByCustomerId(@Param("customerId") Integer customerId);

    @Query("SELECT o FROM Order o " +
            "WHERE (o.status = 'Served' OR o.status = 'Canceled') " +
            "AND (cast(:startDateTime as timestamp) IS NULL OR o.createdAt >= :startDateTime) " +
            "AND (cast(:endDateTime as timestamp) IS NULL OR o.createdAt <= :endDateTime) " +
            "AND ( " +
            "  :userRole = 'ADMIN' " + // 1. Nếu là ADMIN, điều kiện này đúng, trả về tất cả
            "  OR (:userRole = 'CASHIER' AND o.staff = :currentUser) " + // 2. Nếu là Cashier, chỉ xem order họ tạo
            "  OR (:userRole = 'BARISTA' AND o.madeBy = :currentUser) " + // 3. Nếu là Barista, chỉ xem order họ làm
            "  OR (:userRole = 'WAITER' AND o.servedBy = :currentUser) " + // 4. Nếu là Waiter, chỉ xem order họ phục vụ
            ")")
    Page<Order> findCompletedOrdersByDateRange(
            @Param("currentUser") Staff currentUser,
            @Param("userRole") String userRole,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);

    @Query("SELECT o FROM Order o " +
            "WHERE (o.status = 'Pending' OR o.status = 'Ready') " + // <-- Chỉ lấy Pending/Ready
            "AND ( " +
            "  :userRole = 'ADMIN' " + // 1. ADMIN thấy hết
            "  OR (:userRole = 'CASHIER' AND o.staff = :currentUser) " + // 2. Cashier chỉ thấy order mình tạo
            "  OR (:userRole = 'BARISTA') " + // 3. Barista thấy hết (để làm)
            "  OR (:userRole = 'WAITER') " + // 4. Waiter thấy hết (để phục vụ)
            ")")
    Page<Order> findActiveOrdersForUser(
            @Param("currentUser") Staff currentUser,
            @Param("userRole") String userRole,
            Pageable pageable);

    Page<Order> findByCustomerCusIdOrderByCreatedAtDesc(int customerId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.table = :table AND o.status IN ('Pending', 'Served')")
    Order findActiveOrder(@Param("table") Table table);

    Order findFirstByTable(Table table);

    @Query("SELECT o FROM Order o WHERE o.customer = :customer AND o.table IS NOT NULL AND o.status NOT IN ('Served', 'Canceled') ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByCustomer(@Param("customer") Customer customer);

    @Query("SELECT o FROM Order o WHERE o.customer = :customer AND o.table.status = 'occupied' ORDER BY o.createdAt DESC")
    List<Order> findOrderByCustomerAndOccupiedTable(@Param("customer") Customer customer);
}


