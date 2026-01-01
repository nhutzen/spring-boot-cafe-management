package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.services.CustomerService;

import java.time.LocalDate;
import java.time.Period;
import vn.edu.fpt.cafemanagement.services.OtpService;
import vn.edu.fpt.cafemanagement.services.PasswordService;

@Controller
public class RegisterController {
    private OtpService otpService;
    private PasswordService passwordService;
    private final CustomerService customerService;

    public RegisterController(OtpService otpService, PasswordService passwordService, CustomerService customerService) {
        this.otpService = otpService;
        this.passwordService = passwordService;
        this.customerService = customerService;
    }

    @GetMapping(path = "/register")
    public String showRegister(Model model){
        model.addAttribute("customer", new Customer());
        return "account/register";
    }


    @PostMapping(path = "/register")
    public String doRegister(Model model, @ModelAttribute Customer customer, HttpSession session){
        String hashPassword = BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt());
        customer.setPassword(hashPassword);
        customer.setImg("avatar.jpeg");
        session.setAttribute("pendingCustomer", customer);

        String fullName = customer.getName();
        String username = customer.getUsername();
        String phoneNumber = customer.getPhoneNumber();
        String email = customer.getEmail();
        String password = customer.getPassword();
        LocalDate dob = customer.getDateOfBirth();

        if (fullName == null || fullName.isBlank()) {
            model.addAttribute("errorMessage", "Full name cannot be null or blank");
            return "account/register";
        }
        if (!fullName.matches("^[\\p{L} ]+$")) {
            model.addAttribute("errorMessage", "Full name can only contain letters");
            return "account/register";
        }

        if (username == null || username.isBlank()) {
            model.addAttribute("errorMessage", "Username cannot be null or blank");
            return "account/register";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            model.addAttribute("errorMessage", "Username can only contain letters, digits, and underscores");
            return "account/register";
        }
        if (customerService.findByUsername(username) != null) {
            model.addAttribute("errorMessage", "Username is already in use");
            return "account/register";
        }

        if (email == null || email.isBlank()) {
            model.addAttribute("errorMessage", "Email cannot be null or blank");
            return "account/register";
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            model.addAttribute("errorMessage", "Invalid email format");
            return "account/register";
        }
        if (customerService.findByEmail(email) != null) {
            model.addAttribute("errorMessage", "Email is already registered");
            return "account/register";
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            model.addAttribute("errorMessage", "Phone number cannot be null or blank");
            return "account/register";
        }
        if (!phoneNumber.matches("\\d{10}")) {
            model.addAttribute("errorMessage", "Phone number must contain exactly 10 digits");
            return "account/register";
        }
        if (phoneNumber.equals("0000000000")) {
            model.addAttribute("errorMessage", "Phone number cannot be all zeros");
            return "account/register";
        }
        if (customerService.findByPhoneNumber(phoneNumber) != null) {
            model.addAttribute("errorMessage", "Phone number is already in use");
            return "account/register";
        }

        if (dob == null) {
            model.addAttribute("errorMessage", "Date of birth cannot be null");
            return "account/register";
        }
        LocalDate today = LocalDate.now();
        int age = Period.between(dob, today).getYears();
        if (age < 15 || age > 100) {
            model.addAttribute("errorMessage", "Age must be between 15 and 100 years old");
            return "account/register";
        }

        if (password == null || password.isBlank()) {
            model.addAttribute("errorMessage", "Password cannot be null or blank");
            return "account/register";
        }
        if (password.length() < 8 || password.length() > 64) {
            model.addAttribute("errorMessage", "Password must be between 8 and 64 characters");
            return "account/register";
        }
        if (password.contains(" ")) {
            model.addAttribute("errorMessage", "Password must not contain spaces");
            return "account/register";
        }
        if (!password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+")) {
            model.addAttribute("errorMessage", "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
            return "account/register";
        }

        passwordService.sendOtpForRegister(customer.getEmail());

        return "redirect:/verify-email";

    }

    @GetMapping("/verify-email")
    public String showVerifyOtp(HttpSession session, Model model) {
        Customer pendingCustomer = (Customer) session.getAttribute("pendingCustomer");

        if (pendingCustomer == null) {
            return "redirect:/register?error=expired";
        }

        model.addAttribute("email", pendingCustomer.getEmail());
        return "account/verify-email";
    }


    @PostMapping("/verify-email")
    public String verifyOtp(String otp,
                            Model model,
                            HttpSession session) {

        Customer pendingCustomer = (Customer) session.getAttribute("pendingCustomer");

        if (pendingCustomer == null) {
            model.addAttribute("errorMessage", "Session has expired!");
            return "account/register";
        }

        boolean valid = otpService.validateOtp(pendingCustomer.getEmail(), otp);

        if (!valid) {
            return "redirect:/login?errorMessage=The OTP code is incorrect or has expired!";
        }

        try {
            customerService.save(pendingCustomer);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "account/verify-email";
        }

        session.removeAttribute("pendingCustomer");

        return "redirect:/login?successMessage=Your account has been successfully registered. Welcome aboard!";
    }

}
