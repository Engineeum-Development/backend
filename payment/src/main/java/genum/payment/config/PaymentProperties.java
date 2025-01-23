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
    private String apiKey;
    private String gatewayUrl;
    private String paymentImpl;
}
