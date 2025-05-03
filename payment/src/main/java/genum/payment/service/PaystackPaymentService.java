package genum.payment.service;

import genum.payment.config.PaymentProperties;
import genum.payment.constant.PaymentPlatform;
import genum.payment.domain.PaystackWebhook;
import genum.payment.domain.WebHook;
import genum.payment.event.EventType;
import genum.payment.event.PaymentEvent;
import genum.payment.model.CoursePayment;
import genum.payment.repository.PaymentRepository;
import genum.product.service.ProductService;
import genum.shared.payment.constants.PaymentStatus;
import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.PaymentResponseData;
import genum.shared.payment.domain.ProductRequest;
import genum.shared.payment.exception.PaymentNotFoundException;
import genum.shared.payment.exception.PaymentUserAuthenticationFailed;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static genum.payment.domain.PaystackDomain.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackPaymentService implements PaymentService {

    public static final String PAYMENT_BASE_URL = "https://api.paystack.co/";
    public static final String INITIALIZE_TRANSACTION_URL = PAYMENT_BASE_URL + "/transaction/initialize";
    public static final String VERIFY_TRANSACTION_URL = PAYMENT_BASE_URL + "transaction/verify/";
    public static final String LIST_TRANSACTIONS_URL = PAYMENT_BASE_URL + "/transaction";
    public static final String FETCH_TRANSACTION_URL = PAYMENT_BASE_URL + "/transaction/";
    private final RestTemplate restTemplate;
    private final ProductService productService;
    private final PaymentRepository paymentRepository;

    private final PaymentProperties paymentProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public PaymentResponse initializePayment(ProductRequest productRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PaymentUserAuthenticationFailed();
        }
        var user = (CustomUserDetails) authentication.getPrincipal();
        CourseDTO course = productService.findCourseByReference(productRequest.productId());
        String transactionRef = UUID.randomUUID().toString();
        var amountInBaseUnits = course.price() * 100;


        InitializeTransaction initializeTransaction = InitializeTransaction.builder()
                .amount(String.valueOf(amountInBaseUnits))
                .email(user.getEmail())
                .reference(transactionRef)
                .currency("NGN")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paymentProperties.getPaystack_ApiKey());
        headers.set("Content-Type", "application/json");
        log.info("Authorization: {}", paymentProperties.getPaystack_ApiKey());

        HttpEntity<InitializeTransaction> initializeTransactionHttpEntity = new HttpEntity<>(initializeTransaction, headers);
        var response = restTemplate.exchange(INITIALIZE_TRANSACTION_URL, HttpMethod.POST, initializeTransactionHttpEntity, InitializeTransactionResponse.class);

        String transactionReference = null;
        if (response.getStatusCode().is2xxSuccessful()){

            if (Objects.requireNonNull(response.getBody()).status().equals("true")) {
                var payment = CoursePayment.builder()
                        .courseId(course.referenceId())
                        .paymentInitializationDate(LocalDateTime.now().toString())
                        .paymentValue(course.price())
                        .userid(user.getEmail())
                        .paymentPlatForm(PaymentPlatform.PAYSTACK)
                        .paymentStatus(PaymentStatus.PENDING)
                        .transactionReference(transactionRef)
                        .build();
                transactionRef = paymentRepository.save(payment).getTransactionReference();
                return new PaymentResponse(LocalDateTime.now().toString(),
                        PaymentStatus.PENDING,
                        PaymentResponseData.builder()
                                .message("Payment initialization was a success")
                                .authorizationUrl(response.getBody().data().authorizationUrl())
                                .accessCode(response.getBody().data().accessCode())
                                .reference(transactionRef)
                                .build());
            } else {
                return new PaymentResponse(LocalDateTime.now().toString(), PaymentStatus.FAILED, PaymentResponseData.builder().message("Payment failed: Try Again").build());
            }

        } else {
            return new PaymentResponse(LocalDateTime.now().toString(), PaymentStatus.FAILED, PaymentResponseData.builder().message("Payment failed: Was not able to reach the provider please try again later").build());
        }

    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(String transactionReference) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PaymentUserAuthenticationFailed();
        }
        var payment = paymentRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(PaymentNotFoundException::new);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + paymentProperties.getPaystack_ApiKey());
        headers.set("Content-Type", "application/json");

        var initializeTransactionHttpEntity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(VERIFY_TRANSACTION_URL+transactionReference,HttpMethod.GET,initializeTransactionHttpEntity, VerifyTransactionResponse.class);
        if (response.getStatusCode().is2xxSuccessful()){
            var responseData = Objects.requireNonNull(response.getBody()).data();
            assert responseData != null;
            if (responseData.status().equals("success")) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment = paymentRepository.save(payment);

                var paymentEvent = new PaymentEvent(
                        payment.toPaymentDTO(),
                        EventType.PAYMENT_SUCCESSFUL,
                        null
                );
                applicationEventPublisher.publishEvent(paymentEvent);


                return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), PaymentResponseData.builder().message("Payment Success: Congratulations you can now access the course").build());

            } else if (responseData.status().equals("failed")) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), PaymentResponseData.builder().message("Payment Failed: Please Try again").build());
            } else {
                return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), PaymentResponseData.builder().message("Payment Pending: Please check back later").build());
            }

        } else {
            return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), PaymentResponseData.builder().message("Verification failed: Please try again").build());
        }

    }

    @Override
    public String handleWebHook(WebHook webhook){

        PaystackWebhook paystackWebhook = (PaystackWebhook) webhook;
        if (paystackWebhook.event().equalsIgnoreCase("charge.success")) {
            if (paystackWebhook.data().status().equalsIgnoreCase("success")){
                int amountPayed = paystackWebhook.data().amount();
                String transactionRef = paystackWebhook.data().reference();
                CoursePayment payment =  paymentRepository.findByTransactionReference(transactionRef).orElseThrow(PaymentNotFoundException::new);
                CourseDTO courseDTO = productService.findCourseByReference(payment.getCourseId());
                if (courseDTO.price() == amountPayed) {
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                    paymentRepository.save(payment);

                    var paymentEvent = new PaymentEvent(
                            payment.toPaymentDTO(),
                            EventType.PAYMENT_SUCCESSFUL,
                            null
                    );
                    applicationEventPublisher.publishEvent(paymentEvent);
                    return "ok";
                }
                return "ok";
            }
            return "failed";
        }
        return "unsupported-event";
    }

}
