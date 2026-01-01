package vn.edu.fpt.cafemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cafemanagement.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}
