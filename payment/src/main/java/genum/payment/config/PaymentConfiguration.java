package genum.payment.config;

import genum.payment.repository.PaymentRepository;
import genum.payment.service.PaymentService;
import genum.payment.service.FlutterWavePaymentService;
import genum.payment.service.PaystackPaymentService;
import genum.product.service.ProductService;
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
    public PaymentService paystack(RestTemplate restTemplate, ProductService productService, PaymentRepository paymentRepository, PaymentProperties paymentProperties) {
        return new PaystackPaymentService(restTemplate, productService,paymentRepository, paymentProperties);
    }

}


