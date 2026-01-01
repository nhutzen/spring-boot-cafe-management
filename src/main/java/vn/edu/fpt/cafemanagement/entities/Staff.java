package vn.edu.fpt.cafemanagement.entities;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
public class Staff implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private int managerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "date_of_birth")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @Column(name = "img")
    private String img;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<Order> orders;


    public Staff() {
    }

    public Staff(int managerId, String name, String phoneNumber, String email, String address, String password,
                 String username, Role role, LocalDate dateOfBirth, String img, boolean isActive,
                 List<Order> orders) {
        this.managerId = managerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.password = password;
        this.username = username;
        this.role = role;
        this.dateOfBirth = dateOfBirth;
        this.img = img;
        this.isActive = isActive;
        this.orders = orders;
//        this.shiftAssignments = shiftAssignments;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }


}
