package vn.edu.fpt.cafemanagement.services;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomOidcUserService extends OidcUserService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOidcUserService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String googleId = oidcUser.getAttribute("sub");
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getAttribute("picture");

        System.out.println(email);

        Optional<Customer> existingCustomer = customerRepository.findByEmail(email);
        System.out.println("get Customer ok");
        Customer customer;


        if (existingCustomer.isPresent()) {
            customer = existingCustomer.get();
            customer.setImg(picture);
            customer.setGoogleAccount(true);
        } else {
            customer = new Customer();
            customer.setUsername(googleId);
            customer.setEmail(email);
            customer.setName(name);
            customer.setImg(picture);
            customer.setPoint(Integer.valueOf(0));
            customer.setGoogleAccount(true);

            String randomPassword = UUID.randomUUID().toString();
            customer.setPassword(passwordEncoder.encode(randomPassword));
        }

        customerRepository.save(customer);

        Set<SimpleGrantedAuthority> mappedAuthorities = Set.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));


        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
