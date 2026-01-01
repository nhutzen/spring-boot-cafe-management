//package vn.edu.fpt.cafemanagement.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import vn.edu.fpt.cafemanagement.entities.Customer;
//import vn.edu.fpt.cafemanagement.entities.PointHistory;
//import vn.edu.fpt.cafemanagement.security.LoggedUser;
//import vn.edu.fpt.cafemanagement.services.CustomerService;
//
//import java.util.List;
//
//@Controller
//@RequestMapping(value = "/profile")
//public class CustomerController {
//    @Autowired
//    private CustomerService customerService;
//    @Autowired
//    private LoggedUser loggedUser;
//
//    @RequestMapping(value = "")
//    public String viewProfile(Model model) {
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
//            return "redirect:/login";
//        }
//        Customer sessionCustomer = loggedUser.getLoggedCustomer();
//        if (sessionCustomer == null) {
//            return "redirect:/login";
//        }
//        Customer customer = customerService.getCustomerById(sessionCustomer.getCusId());
//        model.addAttribute("customer", customer);
//        return "profile/view";
//    }
//
//    @RequestMapping(value = "/pointhistory")
//    public String viewPointHistory(Model model) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
//            return "redirect:/login";
//        }
//        Customer sessionCustomer = loggedUser.getLoggedCustomer();
//        if (sessionCustomer == null) {
//            return "redirect:/login";
//        }
//
//        int cusId = sessionCustomer.getCusId();
//        List<PointHistory> pointHistoryList = customerService.getPointHistoryByCustomerId(cusId);
//        model.addAttribute("pointHistoryList", pointHistoryList);
//        return "profile/pointHistory";
//    }
//
//    @GetMapping("/edit")
//    public String editProfile(Model model) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
//            return "redirect:/login";
//        }
//        Customer sessionCustomer = loggedUser.getLoggedCustomer();
//        if (sessionCustomer == null) {
//            return "redirect:/login";
//        }
//        Customer customer = customerService.getCustomerById(sessionCustomer.getCusId());
//        model.addAttribute("customer", customer);
//        return "profile/edit";
//    }
//
//    @PostMapping("/edit")
//    public String editProfile(@ModelAttribute(name = "customer") Customer customer, @RequestParam(value = "imgFile", required = false) MultipartFile imgFile, Model model) {
//        try {
//            Customer sessionCustomer = loggedUser.getLoggedCustomer();
//            if (customer.getCusId() != sessionCustomer.getCusId()) {
//                System.err.println("SECURITY ALERT: Customer ID mismatch! Form ID = "
//                        + customer.getCusId() + ", Logged ID = " + sessionCustomer.getCusId());
//
//                model.addAttribute("title", "Security Error");
//                model.addAttribute("errorMessage", "Update request denied due to invalid data.");
//                return "error-page";
//            }
//            if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isEmpty()) {
//                return "redirect:/profile/edit";
//            }
//            customerService.updateCustomer(customer, imgFile);
//            return "redirect:/profile";
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("errorMessage", e.getMessage());
//            model.addAttribute("customer", customer);
//            return "profile/edit";
//        }
//    }
//
//    @RequestMapping(value = "/changePassword")
//    public String changePassword(Model model) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
//            return "redirect:/login";
//        }
//        Customer customer = loggedUser.getLoggedCustomer();
//        if (customer == null) {
//            return "redirect:/login";
//        }
//        model.addAttribute("customer", customer);
//        return "profile/changePassword";
//    }
//
//    @PostMapping("/changePassword")
//    public String changePassword(@ModelAttribute(name = "customer") Customer customer, @RequestParam(value = "currentPassword") String currentPassword, @RequestParam(value = "newPassword") String newPassword, @RequestParam(value = "confirmPassword") String confirmPassword, Model model) {
//        try {
//            customerService.changePassword(customer.getCusId(), newPassword, confirmPassword, currentPassword);
//            return "redirect:/profile";
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("errorMessage", e.getMessage());
//            model.addAttribute("customer", customer);
//            return "profile/changePassword";
//        }
//    }
//
//
//}
package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.cafemanagement.entities.CustomUserDetails;
import vn.edu.fpt.cafemanagement.entities.Customer;
import vn.edu.fpt.cafemanagement.entities.Order;
import vn.edu.fpt.cafemanagement.entities.PointHistory;
import vn.edu.fpt.cafemanagement.repositories.CustomerRepository;
import vn.edu.fpt.cafemanagement.security.LoggedUser;
import vn.edu.fpt.cafemanagement.services.CustomerService;
import vn.edu.fpt.cafemanagement.services.OrderService;

import java.util.List;

@Controller
@RequestMapping(value = "/profile")
public class CustomerController {

    private final CustomerService customerService;
    private final LoggedUser loggedUser;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;

    public CustomerController(CustomerService customerService,
                              LoggedUser loggedUser,
                              CustomerRepository customerRepository,
                              OrderService orderService) {
        this.customerService = customerService;
        this.loggedUser = loggedUser;
        this.customerRepository = customerRepository;
        this.orderService = orderService;
    }

    @RequestMapping(value = "")
    public String viewProfile(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer sessionCustomer = loggedUser.getLoggedCustomer();
        if (sessionCustomer == null) {
            return "redirect:/login";
        }
        Customer customer = customerService.getCustomerById(sessionCustomer.getCusId());
        model.addAttribute("customer", customer);
        return "profile/view";
    }

    @GetMapping(value = "/point-history")
    public String viewPointHistory(@RequestParam(value = "page", defaultValue = "1") int page, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer sessionCustomer = loggedUser.getLoggedCustomer();
        if (sessionCustomer == null) {
            return "redirect:/login";
        }

        int cusId = sessionCustomer.getCusId();
        int pageSize = 5;

        Page<PointHistory> historyPage = customerService.getPointHistoryByCustomerId(cusId, page, pageSize);

        model.addAttribute("pointHistoryList", historyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());

        return "profile/point-history";
    }

    @GetMapping(value = "/order-history")
    public String viewOrderHistory(Model model,
                                   @RequestParam(value = "page", defaultValue = "1") int page) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer sessionCustomer = loggedUser.getLoggedCustomer();
        if (sessionCustomer == null) {
            return "redirect:/login";
        }

        int cusId = sessionCustomer.getCusId();
        int pageSize = 5;

        Page<Order> orderPage = orderService.getOrderHistoryByCustomerId(cusId, page, pageSize);

        model.addAttribute("orderHistoryList", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        return "profile/order-history";
    }

    @GetMapping("/edit")
    public String editProfile(Model model, HttpSession httpSession) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String reminder = (String) httpSession.getAttribute("profileReminder");
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer sessionCustomer = loggedUser.getLoggedCustomer();
        if (sessionCustomer == null) {
            return "redirect:/login";
        }

        if (reminder != null) {
            model.addAttribute("reminder", reminder);
            httpSession.removeAttribute("profileReminder");
        }
        Customer customer = customerService.getCustomerById(sessionCustomer.getCusId());
        model.addAttribute("customer", customer);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String editProfile(@ModelAttribute(name = "customer") Customer customer, @RequestParam(value = "imgFile", required = false) MultipartFile imgFile, Model model) {
        Customer sessionCustomer = null;
        try {
            sessionCustomer = loggedUser.getLoggedCustomer();
            if (customer.getCusId() != sessionCustomer.getCusId()) {
                System.err.println("SECURITY ALERT: Customer ID mismatch! Form ID = "
                        + customer.getCusId() + ", Logged ID = " + sessionCustomer.getCusId());

                model.addAttribute("title", "Security Error");
                model.addAttribute("errorMessage", "Update request denied due to invalid data.");
                return "error-page";
            }
            if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isEmpty()) {
                return "redirect:/profile/edit";
            }
            customerService.updateCustomer(customer, imgFile);

            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customer", sessionCustomer);
            return "profile/edit";
        }
    }

    @RequestMapping(value = "/changePassword")
    public String changePassword(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }
        Customer customer = loggedUser.getLoggedCustomer();
        if (customer == null) {
            return "redirect:/login";
        }
        model.addAttribute("customer", customer);
        return "profile/changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@ModelAttribute(name = "customer") Customer customer, @RequestParam(value = "currentPassword") String currentPassword, @RequestParam(value = "newPassword") String newPassword, @RequestParam(value = "confirmPassword") String confirmPassword, Model model) {
        try {
            customerService.changePassword(customer.getCusId(), newPassword, confirmPassword, currentPassword);
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("customer", customer);
            return "profile/changePassword";
        }
    }


}