package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Role;
import vn.edu.fpt.cafemanagement.repositories.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleById(int id) {
        return roleRepository.findById(id);
    }

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    public void deleteRole(int id) {
        roleRepository.deleteById(id);
    }
}
