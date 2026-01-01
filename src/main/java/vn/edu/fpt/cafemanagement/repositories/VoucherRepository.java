package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Voucher;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    List<Voucher> findByIsActiveTrue();
    List<Voucher> findByIsActiveFalse();
    Voucher findByVoucherId(int voucherId);

    void deleteByVoucherId(int voucherId);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
            "AND v.quantity > 0 " +
            "AND v.startDate <= :currentDate " +
            "AND v.endDate >= :currentDate")
    List<Voucher> findApplicableVouchers(@Param("currentDate") LocalDate currentDate);
}
