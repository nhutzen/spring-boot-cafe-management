package vn.edu.fpt.cafemanagement.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerUserDetailsService.class);

    private final CustomerRepository customerRepository;

    public CustomerUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("=== CUSTOMER LOGIN ATTEMPT ===");
        logger.info("Attempting to load customer: {}", username);

        try {
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("Customer not found with username: {}", username);
                        return new UsernameNotFoundException("Customer not found with username: " + username);
                    });

            logger.info("Customer found: {}", customer.getUsername());
            logger.info("Customer ID: {}", customer.getCusId());
            logger.info("Password from DB: {}", customer.getPassword() != null ? "EXISTS" : "NULL");

            CustomUserDetails userDetails = new CustomUserDetails(customer);
            logger.info("CustomUserDetails created successfully");
            logger.info("Authorities: {}", userDetails.getAuthorities());

            return userDetails;
        } catch (Exception e) {
            logger.error("Error loading customer: {}", e.getMessage(), e);
            throw e;
        }
    }
}