package genum.payment.config;

import genum.payment.service.PaymentService;
import genum.serviceimplementation.payment.FlutterWavePaymentService;
import genum.serviceimplementation.payment.PaystackPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PaymentConfiguration {

    @Bean(name = "flutter_wave")
    public PaymentService flutterWavePayment() {
        return new FlutterWavePaymentService();
    }

    @Bean(name = "paystack")
    public PaymentService paystack() {
        return new PaystackPaymentService();
    }
}
