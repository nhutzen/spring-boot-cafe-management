package vn.edu.fpt.cafemanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;
import vn.edu.fpt.cafemanagement.security.LoggedUser;

import java.io.IOException;

@Component
public class ProfileCompletionFilter extends OncePerRequestFilter {

    private LoggedUser loggedUser;
    private CustomerRepository customerRepository;

    public ProfileCompletionFilter(LoggedUser loggedUser, CustomerRepository customerRepository) {
        this.loggedUser = loggedUser;
        this.customerRepository = customerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();


        boolean allowed =
                uri.startsWith("/profile/edit") ||
                        uri.startsWith("/home") ||
                        uri.startsWith("/css") ||
                        uri.startsWith("/js") ||
                        uri.startsWith("/images") ||
                        uri.startsWith("/https://lh3.googleusercontent.com") ||
                        uri.startsWith("/assets");

        Customer user = loggedUser.getLoggedCustomer();

        if (user != null ) {
            user = customerRepository.getCustomerByCusId(user.getCusId());
            if(user.getPhoneNumber() == null || user.getDateOfBirth() == null){
                if (!allowed) {
                    response.sendRedirect("/profile/edit");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
