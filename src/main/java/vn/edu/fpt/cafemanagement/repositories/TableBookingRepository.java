package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Table;
import vn.edu.fpt.cafemanagement.entities.TableBooking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, Integer> {

    List<TableBooking> findByCustomer_CusIdOrderByBookingTimeDesc(int customerId);

    Page<TableBooking> findByCustomer_CusIdOrderByBookingTimeDesc(Integer customerId, Pageable pageable);

    Page<TableBooking> findByCustomer_CusIdAndBookingTimeBetweenOrderByBookingTimeDesc(int customerCusId, LocalDateTime bookingTimeAfter, LocalDateTime bookingTimeBefore, Pageable pageable);

    Page<TableBooking> findByCustomer_CusIdAndStatusAndBookingTimeBetweenOrderByBookingTimeDesc(int customerCusId, String status, LocalDateTime bookingTimeAfter, LocalDateTime bookingTimeBefore, Pageable pageable);

    Page<TableBooking> findByCustomer_CusIdAndStatusOrderByBookingTimeDesc(int customerCusId, String status, Pageable pageable);

    Page<TableBooking> findAllByOrderByBookingTimeDesc(Pageable pageable);

    Page<TableBooking> findByStatusOrderByBookingTimeDesc(String status, Pageable pageable);

    Page<TableBooking> findByBookingTimeBetweenOrderByBookingTimeDesc(LocalDateTime bookingTimeAfter, LocalDateTime bookingTimeBefore, Pageable pageable);

    Page<TableBooking> findByStatusAndBookingTimeBetweenOrderByBookingTimeDesc(String status, LocalDateTime bookingTimeAfter, LocalDateTime bookingTimeBefore, Pageable pageable);

    @Query(value="select * from TableBooking where table_id = :tableId and status = 'checked-in'", nativeQuery = true)
    List<TableBooking> findActiveBookingByTable(int tableId);

    @Query(value="select * from tablebooking where booking_time < :expiredTime and status in ('booked','pending')", nativeQuery = true)
    List<TableBooking> findExpiredBooking(LocalDateTime expiredTime);

    @Query("SELECT COUNT(b) FROM TableBooking b " +
            "WHERE b.customer.cusId = :customerId " +
            "AND CAST(b.bookingTime AS date) = :date")
    int countByCustomerAndDate(@Param("customerId") int customerId,
                               @Param("date") LocalDate date);

    TableBooking findFirstByCustomerCusIdAndStatus(int cusId, String status);

    TableBooking findFirstByTableAndStatus(Table table, String status);

    TableBooking findFirstByTable(Table table);

    @Query("SELECT tb.table FROM TableBooking tb WHERE tb.customer.cusId = :cusId AND tb.status = :status")
    List<Table> findTableByCustomer_CusIdAndStatus(int cusId, String status);

//    List<TableBooking> findExpiredBookings();
}