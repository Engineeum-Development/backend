package genum.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentProperties {
    private String paystack_ApiKey;
    private String paystack_CallbackUrl;
    private String flutterWave_SecretKey;
    private String flutterWave_PublicKey;
    private String flutterWave_EncryptionKey;
    private String flutterWave_CallBackUrl;
}
