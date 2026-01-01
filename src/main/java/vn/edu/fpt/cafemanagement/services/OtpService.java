package vn.edu.fpt.cafemanagement.services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }

    public String generateOtp(String email) {
        // Sinh OTP 6 chữ số (có thể có 0 ở đầu)
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(15)));
        System.out.println("[DEBUG] Generated OTP for " + email + " = " + otp);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpData data = otpStorage.get(email);
        System.out.println("[DEBUG] Input OTP: " + otp + " | Stored OTP: " + data.getOtp());
        if (data == null) {
            return false;
        }

        // Nếu OTP đã hết hạn -> xóa và trả false
        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpStorage.remove(email);
            return false;
        }

        // So sánh OTP
        boolean isValid = data.getOtp().equals(otp);

        // Nếu hợp lệ thì xóa (chỉ dùng 1 lần)
        if (isValid) {
            otpStorage.remove(email);
        }

        return isValid;
    }
}


