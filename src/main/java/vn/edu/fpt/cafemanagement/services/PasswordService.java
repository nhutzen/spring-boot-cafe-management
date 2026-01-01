package vn.edu.fpt.cafemanagement.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;

@Service
public class PasswordService {
    CustomerRepository customerRepository;
    OtpService otpService;
    JavaMailSender mailSender;
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public PasswordService(CustomerRepository customerRepository, OtpService otpService,JavaMailSender mailSender) {
        this.customerRepository = customerRepository;
        this.otpService = otpService;
        this.mailSender = mailSender;
    }

    public void sendOtpForRegister(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("The email cannot be empty!");
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Email format is invalid! Example: example@gmail.com");
        }

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("trùng lặp");
        }

        String otp = otpService.generateOtp(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password reset code (valid for 15 minutes)");
        message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in 15 minutes!");

        mailSender.send(message);
    }

    public void sendOtpToEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("The email cannot be empty!");
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Email format is invalid! Example: example@gmail.com");
        }

        customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email does not exist!"));

        String otp = otpService.generateOtp(email);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password reset code (valid for 15 minutes)");
        message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in 15 minutes!");

        mailSender.send(message);
    }

    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
        if (otp == null || otp.isEmpty()) {
            throw new IllegalArgumentException("The otp cannot be empty.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("The new password cannot be empty.");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("The confirm password cannot be empty.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password does not match!");
        }
        if (!otpService.validateOtp(email, otp)) {
            throw new IllegalArgumentException("The OTP code is incorrect or has expired!");
        }
        if (!newPassword.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and include uppercase letters, " +
                    "lowercase letters, numbers, and special characters!\n");
        }
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found!"));
        customer.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        customerRepository.save(customer);
    }
}
