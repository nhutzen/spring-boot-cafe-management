package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private int roleId;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<Staff> staff;


    public Role() {
    }

    public Role(int roleId, String roleName, List<Staff> staff) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.staff = staff;

    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Staff> getStaff() {
        return staff;
    }

    public void setStaff(List<Staff> staff) {
        this.staff = staff;
    }

//    public List<Shift> getShifts() {
//        return shifts;
//    }
//
//    public void setShifts(List<Shift> shifts) {
//        this.shifts = shifts;
//    }

//    public List<Shift> getShiftsRoles() {
//        return shiftsRoles;
//    }
//
//    public void setShiftsRoles(List<Shift> shiftsRoles) {
//        this.shiftsRoles = shiftsRoles;
//    }

}
