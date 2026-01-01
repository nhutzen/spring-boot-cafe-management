package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Staff;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Staff> findByUsername(String username);

    Optional<Staff> findByUsernameAndIsActive(String username, boolean isActive);

    Optional<Staff> findActiveByUsername(String username);
    

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findByPhoneNumber(String phoneNumber);


    Page<Staff> findByIsActiveTrue(Pageable pageable);

    Page<Staff> findByIsActiveFalse(Pageable pageable);

    // Search có phân trang
    @Query("SELECT m FROM Staff m LEFT JOIN m.role r WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Staff> search(@Param("keyword") String keyword, Pageable pageable);

    // Lấy danh sách nhân viên còn active
    List<Staff> findByIsActiveTrue();

    // Lấy danh sách nhân viên đã xóa
    List<Staff> findByIsActiveFalse();




}
