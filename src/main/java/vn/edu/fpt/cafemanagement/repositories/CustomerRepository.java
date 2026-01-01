package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.PointHistory;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer getCustomerByCusId(int cusId);

    Optional<Customer> findByUsername(String name);
    Optional<Customer> findByEmail(String email);

    Customer findByPhoneNumber(String phoneNumber);

    @Query(
            value = "SELECT point_history_id, type_of_change, change_time, amount, cus_id, order_id " +
                    "FROM dbo.pointhistory " +
                    "WHERE cus_id = :cusId " +
                    "ORDER BY change_time DESC",
            nativeQuery = true)
    List<PointHistory> getPointHistoryByCustomerId(@Param("cusId") int cusId);

    boolean existsByEmailIgnoreCase(String email);


//Tặng điểm sinh nhật khách
    @Query("SELECT c FROM Customer c WHERE MONTH(c.dateOfBirth) = :month AND DAY(c.dateOfBirth) = :day")
    List<Customer> findCustomersByBirthday(@Param("month") int month, @Param("day") int day);

}
