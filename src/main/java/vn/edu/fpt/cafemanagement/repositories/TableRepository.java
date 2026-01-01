package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Table;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<Table, Integer> {
    @Query(value = "SELECT table_id, status, capacity FROM [TABLE]", nativeQuery = true)
    List<Table> getTablesList();

    @Query(value = "SELECT table_id, status, capacity FROM [TABLE] WHERE table_id = :id", nativeQuery = true)
    Optional<Table> findById(@Param("id")Integer integer);

    @Query(value = "select distinct capacity from [table]", nativeQuery = true)
    List<Integer> capacityList();

    List<Table> getTableByStatus(String status);
}
