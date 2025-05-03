package genum.genumUser.security.interceptor;

import genum.shared.util.CachedBodyHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PaystackWebHookInterceptor implements WebHookInterceptor{


    @Value("${payment.paystack-apikey}")
    private String paystackSecretKey;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String body = new BufferedReader(request.getReader())
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
        Set<String> paystackWebhookIps = Set.of("52.31.139.75","52.49.173.169","52.214.14.220");
        String hashOfPayload = sign(paystackSecretKey, body);

        boolean isCorrectIp = paystackWebhookIps.contains(request.getRemoteAddr());
        boolean isCorrectHash = hashOfPayload.equals(request.getHeader("x-paystack-signature"));

        return isCorrectHash && isCorrectIp;

    }

    private String sign(String secretKey, String payload) throws Exception {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),"HmacSHA512");
        hmacSha512.init(secretKeySpec);
        byte[] hmacBytes = hmacSha512.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        return bytesToHex(hmacBytes);

    }

    private static String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
