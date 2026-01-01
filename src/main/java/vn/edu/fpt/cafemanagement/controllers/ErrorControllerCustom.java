package vn.edu.fpt.cafemanagement.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class ErrorControllerCustom {

    @GetMapping("/403")
    public String accessDenial(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        } else {
            model.addAttribute("username", "guest");
        }
        model.addAttribute("message", "You are not allowed to access this resource");
        return "error/403";
    }
    @GetMapping("/404")
    public String notFound(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        } else {
            model.addAttribute("username", "guest");
        }
        model.addAttribute("message", "Can not found resource");
        return "error/404";
    }
}
