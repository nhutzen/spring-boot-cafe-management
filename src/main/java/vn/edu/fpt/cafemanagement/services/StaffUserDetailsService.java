package vn.edu.fpt.cafemanagement.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
import vn.edu.fpt.cafemanagement.entities.Staff;
import vn.edu.fpt.cafemanagement.repositories.StaffRepository;

@Service
public class StaffUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(StaffUserDetailsService.class);

    private final StaffRepository staffRepository;

    public StaffUserDetailsService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try {
            Staff staff = staffRepository.findByUsernameAndIsActive(username, true)
                    .orElseThrow(() -> {

                        return new UsernameNotFoundException("Staff not found with username: " + username);
                    });


            CustomUserDetails userDetails = new CustomUserDetails(staff);

            return userDetails;
        } catch (Exception e) {

            throw e;
        }
    }
}