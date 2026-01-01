//package vn.edu.fpt.cafemanagement.security;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
//
//import java.io.IOException;
//import java.util.Collection;
//
//@Component
//public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
//
//    private static final Logger logger = LoggerFactory.getLogger(CustomLoginSuccessHandler.class);
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication)
//            throws IOException, ServletException {
//
//
//        if(authentication.getPrincipal() instanceof OidcUser){
//            response.sendRedirect("/home");
//            return;
//        }
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//
//
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        String redirectUrl = "/home"; // default
//
//        for (GrantedAuthority authority : authorities) {
//            String role = authority.getAuthority();
//            logger.info("Processing role: {}", role);
//
//            if (role.equals("ROLE_ADMIN")) {
//                redirectUrl = "/dashboard";
//                break;
//            } else if (role.equals("ROLE_CASHIER")) {
//                redirectUrl = "/table/booking/management";
//                break;
//            } else if (role.equals("ROLE_CUSTOMER")) {
//                redirectUrl = "/home";
//                break;
//            } else if (role.equals("ROLE_BARISTA")) {
//                redirectUrl = "/order/edit";
//                break;
//            } else if (role.equals("ROLE_WAITER")) {
//                redirectUrl = "/home";
//                break;
//            }
//        }
//
//        logger.info("Redirecting to: {}", redirectUrl);
//        response.sendRedirect(redirectUrl);
//    }
//}

package vn.edu.fpt.cafemanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
import vn.edu.fpt.cafemanagement.entities.Customer;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoggedUser loggedUser;

    public CustomLoginSuccessHandler(LoggedUser loggedUser) {
        this.loggedUser = loggedUser;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {


        if(authentication.getPrincipal() instanceof OidcUser){
            Customer customer = loggedUser.getLoggedCustomer();
            if(customer.getPhoneNumber() == null || customer.getDateOfBirth() == null){
                request.getSession().setAttribute("profileReminder", "Welcome, please fill in your phone number and date of birth for a better experience");
                response.sendRedirect("/profile/edit");
            } else {
                response.sendRedirect("/home");
            }
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        System.out.println(userDetails.getUsername());


        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/home"; // default

        label:
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_ADMIN":
                    redirectUrl = "/dashboard";
                    break label;
                case "ROLE_CASHIER":
                    redirectUrl = "/order/create";
                    break label;
                case "ROLE_CUSTOMER", "ROLE_WAITER":
                    redirectUrl = "/home";
                    break label;
                case "ROLE_BARISTA":
                    redirectUrl = "/order/edit";
                    break label;
            }
        }
        response.sendRedirect(redirectUrl);
    }
}