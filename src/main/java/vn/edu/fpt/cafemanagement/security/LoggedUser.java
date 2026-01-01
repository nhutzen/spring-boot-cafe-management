package vn.edu.fpt.cafemanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Staff;
import vn.edu.fpt.cafemanagement.services.CustomerService;

@Component
public class LoggedUser {


    private final CustomerService customerService;

    public LoggedUser(CustomerService customerService) {
        this.customerService = customerService;
    }

    public CustomUserDetails getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public Customer getLoggedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getCustomer();
        }

        if (principal instanceof OidcUser oidcUser) {
            return customerService.findByEmail(oidcUser.getEmail());
        }

//        CustomUserDetails userDetails = getLoggedUser();
//        if (userDetails != null && userDetails.isCustomer()) {
//            return userDetails.getCustomer();
//        }
        return null;
    }

    public Staff getLoggedManager() {
        CustomUserDetails userDetails = getLoggedUser();

        if (userDetails != null && userDetails.isManager()) {
            return userDetails.getManager();
        }
        return null;
    }

    public boolean isCustomer() {
        CustomUserDetails userDetails = getLoggedUser();
        return userDetails != null && userDetails.isCustomer();
    }

    public boolean isManager() {
        CustomUserDetails userDetails = getLoggedUser();
        return userDetails != null && userDetails.isManager();
    }
}
