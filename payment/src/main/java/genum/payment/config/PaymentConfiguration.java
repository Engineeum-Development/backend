package genum.payment.config;

import genum.course.model.Course;
import genum.payment.repository.PaymentRepository;
import genum.payment.service.PaymentService;
import genum.payment.service.FlutterWavePaymentService;
import genum.payment.service.PaystackPaymentService;
import genum.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages = {"genum.course", "genum.payment"})
public class PaymentConfiguration {



    @Bean(name = "flutter_wave")
    public PaymentService flutterWavePayment(RestTemplate restTemplate,
                                             PaymentProperties paymentProperties,
                                             CourseService courseService,
                                             PaymentRepository paymentRepository, ApplicationEventPublisher applicationEventPublisher) {
        return new FlutterWavePaymentService(paymentProperties,
                courseService,
                restTemplate,
                paymentRepository,
                applicationEventPublisher);
    }


    @Bean(name = "paystack")
    @Primary
    public PaymentService paystack(RestTemplate restTemplate,
                                   CourseService courseService,
                                   PaymentRepository paymentRepository,
                                   PaymentProperties paymentProperties,
                                   ApplicationEventPublisher applicationEventPublisher) {
        return new PaystackPaymentService(restTemplate,
                courseService,
                paymentRepository,
                paymentProperties,
                applicationEventPublisher);
    }

}


