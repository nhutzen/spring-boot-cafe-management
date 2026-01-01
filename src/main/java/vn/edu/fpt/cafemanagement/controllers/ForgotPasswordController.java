package vn.edu.fpt.cafemanagement.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cafemanagement.services.PasswordService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class ForgotPasswordController {

    @Autowired
    private PasswordService passwordService;

    /**
     * Kiểm tra và cập nhật số lần resend OTP trong 1 giờ.
     *
     * @return số lần resend hiện tại sau khi cập nhật
     * @throws IllegalStateException nếu đã vượt quá giới hạn cho phép
     */
    private int handleResendOtpLimit(HttpSession session) {
        Integer resendAttempts = (Integer) session.getAttribute("otpResendAttempts");
        Long firstResendTime = (Long) session.getAttribute("otpFirstResendTime");

        if (resendAttempts == null) resendAttempts = 0;

        long currentTime = System.currentTimeMillis();
        long oneHour = 3600_000L; // 1 giờ = 3600_000 ms

        // Nếu chưa có thời điểm đầu tiên hoặc đã quá 1 giờ từ lần đầu, reset counter
        if (firstResendTime == null || currentTime - firstResendTime > oneHour) {
            resendAttempts = 0;
            firstResendTime = currentTime;
            session.setAttribute("otpFirstResendTime", firstResendTime);
        }

        // Nếu vượt quá giới hạn 3 lần trong 1 giờ
        if (resendAttempts >= 3) {
            throw new IllegalStateException("You have reached the maximum number of OTP resends. Please try again after 1 hour.");
        }

        // Cập nhật lại session
        resendAttempts++;
        session.setAttribute("otpResendAttempts", resendAttempts);

        return resendAttempts;
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "account/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpSession session,
                                 Model model) {
        try {
            session.setAttribute("otpAttempts", 0);
            session.setAttribute("otpEmail", email);

            // ✅ Gọi hàm xử lý resend limit
            handleResendOtpLimit(session);

            // Gửi OTP
            passwordService.sendOtpToEmail(email);

            return "redirect:/set-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            return "redirect:/login?errorMessage=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "account/forgot-password";
        }
    }

    @GetMapping("/resend-otp")
    public String resendOtp(@RequestParam String email,
                            HttpSession session) {
        String sessionEmail = (String) session.getAttribute("otpEmail");
        if (sessionEmail == null || !sessionEmail.equals(email)) {
            return "redirect:/forgot-password?errorMessage=Session expired. Please request a new OTP.";
        }

        try {
            int resendAttempts = handleResendOtpLimit(session);
            passwordService.sendOtpToEmail(email);

            session.setAttribute("otpAttempts", 0);
            int remaining = 3 - resendAttempts;

            String successMsg = "A new OTP has been sent to your email! You can resend " +
                    remaining + " more time" + (remaining > 1 ? "s" : "") + " within 1 hour.";
            return "redirect:/set-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&successMessage=" +
                    URLEncoder.encode(successMsg, StandardCharsets.UTF_8);

        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            return "redirect:/login?errorMessage=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/set-password")
    public String setPassword(@RequestParam(required = false) String email,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Integer attempts = (Integer) session.getAttribute("otpAttempts");
        if (attempts != null && attempts > 0) {
            model.addAttribute("attemptsLeft", 3 - attempts);
        }
        // Validate session
        String sessionEmail = (String) session.getAttribute("otpEmail");
        if (sessionEmail == null || (email != null && !sessionEmail.equals(email))) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Session expired or invalid email.");
            return "redirect:/forgot-password?error=invalidSession";
        }
        model.addAttribute("email", email);
        return "account/set-password";
    }

    @PostMapping("/set-password")
    public String setPassword(@RequestParam String email,
                              @RequestParam String otp,
                              @RequestParam String newPassword,
                              @RequestParam String confirmPassword,
                              HttpSession session,
                              Model model) {
        // Get attemps from session
        Integer attempts = (Integer) session.getAttribute("otpAttempts");
        if (attempts == null) {
            attempts = 0;
        }
        try {
            // Validate email
            String sessionEmail = (String) session.getAttribute("otpEmail");
            if (!email.equals(sessionEmail)) {
                return "account/login?errorMessage=Invalid session.";
            }
            // Kiểm tra đã vượt quá số lần cho phép chưa
            if (attempts >= 3) {
                model.addAttribute("email", email);
                session.removeAttribute("otpAttempts");
                session.removeAttribute("otpEmail");
                String errorMessage = "You have exceeded the maximum number of attempts (3). Please request a new OTP.";
                return "redirect:/login?errorMessage=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            }
            // Tăng số lần thử
            attempts++;
            session.setAttribute("otpAttempts", attempts);
            passwordService.resetPassword(email, otp, newPassword, confirmPassword);
            // Xóa session sau khi thành công
            session.removeAttribute("otpAttempts");
            session.removeAttribute("otpEmail");
            return "redirect:/login?successMessage=Reset password successfully!"; //Chuyen ve trang login
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("email", email);
            return "account/set-password";
        }
    }
}

