package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.PointHistory;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Integer> {
    Page<PointHistory> findByCustomerCusIdOrderByChangeTimeDesc(int customerId, Pageable pageable);
}
