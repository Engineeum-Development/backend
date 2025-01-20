package genum.payment.config;

import genum.payment.repository.ProductRepository;
import genum.payment.service.PaymentService;
import genum.serviceimplementation.payment.FlutterWavePaymentService;
import genum.serviceimplementation.payment.PaystackPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class PaymentConfiguration {

    @Bean(name = "flutter_wave")
    public PaymentService flutterWavePayment() {
        return new FlutterWavePaymentService();
    }

    @Bean(name = "paystack")
    public PaymentService paystack(ProductRepository productRepository, RestTemplate restTemplate) {
        return new PaystackPaymentService(productRepository, restTemplate);
    }
}
