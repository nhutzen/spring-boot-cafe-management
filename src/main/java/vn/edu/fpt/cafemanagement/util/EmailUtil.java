package vn.edu.fpt.cafemanagement.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

//@Component
//public class EmailUtil {
//
//    @Autowired
//    private JavaMailSender javaMailSender;
//
//    public void sendSetPasswordEmail(String email) throws MessagingException {
//
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//
//        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
//
//        mimeMessageHelper.setTo(email);
//
//        mimeMessageHelper.setSubject("Set Password");
//
//        mimeMessageHelper.setText("""
//
//                        <div>
//
//                        <a href="http://localhost:8080/set-password?email=%s" target="_blank"> click link to set password </a>
//
//                        </div>
//
//                        """.formatted(email), true);
//
//        javaMailSender.send(mimeMessage);
//
//    }
//}

@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender javaMailSender;

    // Gửi OTP xác thực quên mật khẩu
    public void sendOtpEmail(String email, String otp) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setTo(email);
        helper.setSubject("Mã xác thực đặt lại mật khẩu");

        // HTML nội dung
        String html = """
                <div style="font-family: Arial, sans-serif; line-height: 1.5;">
                    <h2 style="color: #2b6cb0;">Cafe Management - Đặt lại mật khẩu</h2>
                    <p>Xin chào,</p>
                    <p>Mã xác thực (OTP) của bạn là:</p>
                    <h1 style="color: #e53e3e;">%s</h1>
                    <p>Mã này sẽ hết hạn sau <b>15 phút</b>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>
                    <br>
                    <p>Trân trọng,<br>Đội ngũ Cafe Management</p>
                </div>
                """.formatted(otp);

        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
    }
}
