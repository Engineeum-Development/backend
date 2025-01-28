package genum.payment.config;

import genum.payment.repository.PaymentRepository;
import genum.payment.service.PaymentService;
import genum.payment.service.FlutterWavePaymentService;
import genum.payment.service.PaystackPaymentService;
import genum.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages = {"genum.product", "genum.payment"})
public class PaymentConfiguration {



    @Bean(name = "flutter_wave")
    public PaymentService flutterWavePayment(RestTemplate restTemplate,
                                             PaymentProperties paymentProperties,
                                             ProductService productService,
                                             PaymentRepository paymentRepository, ApplicationEventPublisher applicationEventPublisher) {
        return new FlutterWavePaymentService(paymentProperties,
                productService,
                restTemplate,
                paymentRepository,
                applicationEventPublisher);
    }


    @Bean(name = "paystack")
    @Primary
    public PaymentService paystack(RestTemplate restTemplate,
                                   ProductService productService,
                                   PaymentRepository paymentRepository,
                                   PaymentProperties paymentProperties,
                                   ApplicationEventPublisher applicationEventPublisher) {
        return new PaystackPaymentService(restTemplate,
                productService,
                paymentRepository,
                paymentProperties,
                applicationEventPublisher);
    }

}


