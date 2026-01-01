package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cafemanagement.security.CustomLoginSuccessHandler;

import java.io.IOException;

@Controller
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    public LoginController(AuthenticationManager authenticationManager, CustomLoginSuccessHandler customLoginSuccessHandler) {
        this.authenticationManager = authenticationManager;
        this.customLoginSuccessHandler = customLoginSuccessHandler;
    }

    @GetMapping(path={"/customer/login","/login"})
    public String customerLogin(Model model,
                                @RequestParam(value = "error", required = false) boolean error) {
        if(error){
            model.addAttribute("error", "Invalid username or password");
        }
        return  "account/customerLogin";
    }

    @GetMapping(path="/staff/login")
    public String staffLogin(Model model,
                             @RequestParam(value = "error", required = false) boolean error) {
        if(error){
            model.addAttribute("error", "Invalid username or password");
        }
        return  "account/staffLogin";
    }


    @PostMapping("/staff/login")
    public String doPostStaffLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication auth = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            customLoginSuccessHandler.onAuthenticationSuccess(request, response, auth);
            return null;
        } catch (AuthenticationException | IOException | ServletException e) {
            return "redirect:/staff/login?error=true";
        }
    }

    @PostMapping("/customer/login")
    public String doPostCustomerLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication auth = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            customLoginSuccessHandler.onAuthenticationSuccess(request, response, auth);
            return null;
        } catch (AuthenticationException | IOException | ServletException e) {
            return "redirect:/customer/login?error=true";
        }
    }
}
