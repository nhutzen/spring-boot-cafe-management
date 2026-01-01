package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.OrderItem;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query(value = "SELECT DISTINCT oi.product_id " +
            "FROM orderitem oi " +
            "JOIN [Order] o ON oi.order_id = o.order_id " +
            "WHERE o.cus_id = :customerId",
            nativeQuery = true)
    List<Integer> getProductIdsByCustomerId(int customerId);

    List<OrderItem> findByOrder_OrderId(int orderId);

    void deleteByOrder_OrderId(int orderId);

}