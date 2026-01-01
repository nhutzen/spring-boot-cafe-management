package vn.edu.fpt.cafemanagement.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final Customer customer;
    private final Staff staff;

    public CustomUserDetails(Customer customer) {
        this.customer = customer;
        this.staff = null;
    }

    public CustomUserDetails(Staff staff) {
        this.staff = staff;
        this.customer = null;
    }

    public boolean isCustomer() {
        return this.customer != null;
    }

    public boolean isManager() {
        return this.staff != null;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public Staff getManager() {
        return this.staff;
    }

    // Thêm method để lấy ID
    public Integer getId() {
        if (customer != null) {
            return customer.getCusId();
        }
        if (staff != null) {
            return staff.getManagerId();
        }
        return null;
    }

    // Thêm method để lấy email
    public String getEmail() {
        if (customer != null) {
            return customer.getEmail();
        }
        if (staff != null) {
            return staff.getEmail();
        }
        return null;
    }

    public String getFullName() {
        if (customer != null) {
            return customer.getName();
        }
        if (staff != null) {
            return staff.getName();
        }
        return null;
    }

    // Thêm method để lấy user type
    public String getUserType() {
        return customer != null ? "CUSTOMER" : "MANAGER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (customer != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }
        if (staff != null) {
            String roleName = staff.getRole().getRoleName();
            return List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        return (customer != null) ? customer.getPassword() : staff.getPassword();
    }

    @Override
    public String getUsername() {
        return (customer != null) ? customer.getUsername() : staff.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (customer != null) {
            return customer.isEnabled(); // Nếu Customer có field enabled
        }
        if (staff != null) {
            return staff.isEnabled(); // Nếu Staff có field enabled
        }
        return true;
    }
}