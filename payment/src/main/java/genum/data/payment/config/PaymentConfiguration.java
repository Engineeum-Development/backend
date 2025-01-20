package genum.data.payment.config;

import genum.data.payment.repository.ProductRepository;
import genum.data.payment.service.PaymentService;
import genum.data.serviceimplementation.payment.FlutterWavePaymentService;
import genum.data.serviceimplementation.payment.PaystackPaymentService;
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
