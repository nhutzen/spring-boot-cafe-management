package vn.edu.fpt.cafemanagement.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class SignUtil {
    private static final String HMAC_ALGO = "HmacSHA256";
    private static String SECRET; // static secret

    // Khi Spring khởi động, tự inject giá trị từ application.properties
    public SignUtil(@Value("${app.hmac.secret}") String secret) {
        SECRET = secret;
    }

    public static String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET.getBytes(), HMAC_ALGO);
            mac.init(secretKeySpec);
            byte[] raw = mac.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Error while signing data", e);
        }
    }

    public static boolean verify(String data, String sig) {
        String expected = sign(data);
        return MessageDigest.isEqual(expected.getBytes(), sig.getBytes());
    }
}
