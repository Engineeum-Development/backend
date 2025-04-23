package genum.payment.service;

import genum.payment.config.PaymentProperties;
import genum.payment.constant.PaymentPlatform;
import genum.payment.event.EventType;
import genum.payment.event.PaymentEvent;
import genum.payment.model.CoursePayment;
import genum.payment.repository.PaymentRepository;
import genum.product.service.ProductService;
import genum.shared.payment.constants.PaymentStatus;
import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
import genum.shared.payment.exception.PaymentNotFoundException;
import genum.shared.payment.exception.PaymentUserAuthenticationFailed;
import genum.shared.product.DTO.CourseDTO;
import genum.shared.security.CustomUserDetails;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
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
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaystackPaymentService implements PaymentService {

    public static final String PAYMENT_BASE_URL = "https://api.paystack.co";
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
        CourseDTO course = productService.findCourseById(productRequest.getProductId());

        var amountInBaseUnits = course.price() * 100;


        InitializeTransaction initializeTransaction = InitializeTransaction.builder()
                .amount(String.valueOf(amountInBaseUnits))
                .email(user.getEmail())
                .reference(course.referenceId())
                .currency("USD")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", paymentProperties.getPaystack_apiKey());
        headers.set("Content-Type", "application/json");

        HttpEntity<InitializeTransaction> initializeTransactionHttpEntity = new HttpEntity<>(initializeTransaction, headers);
        var response = restTemplate.exchange(INITIALIZE_TRANSACTION_URL, HttpMethod.POST, initializeTransactionHttpEntity, InitializeTransactionResponse.class);
        String paymentId = null;
        if (response.getStatusCode().is2xxSuccessful()){
            if (Objects.requireNonNull(response.getBody()).status.equals("true")) {
                var payment = CoursePayment.builder()
                        .courseId(course.referenceId())
                        .paymentInitializationDate(LocalDateTime.now().toString())
                        .paymentValue(course.price())
                        .userid(user.getEmail())
                        .paymentPlatForm(PaymentPlatform.PAYSTACK)
                        .paymentStatus(PaymentStatus.PENDING)
                        .build();
                payment = paymentRepository.save(payment);
                paymentId = payment.getId();
                return new PaymentResponse(LocalDateTime.now().toString(),
                        PaymentStatus.PENDING,
                        Map.of("message","Payment initialization was a success",
                                "authorization_url", response.getBody().data.authorizationUrl,
                                "access_code", response.getBody().data().accessCode(),
                                "reference", response.getBody().data().reference()));
            } else {
                return new PaymentResponse(LocalDateTime.now().toString(), PaymentStatus.FAILED, Map.of("message","The payment of %s initialization failed please try again".formatted(paymentId)));
            }

        } else {
            return new PaymentResponse(LocalDateTime.now().toString(), PaymentStatus.FAILED, Map.of("Message", "Was not able to reach the provider please try again later"));
        }

    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(String reference, String paymentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PaymentUserAuthenticationFailed();
        }

        var user = (CustomUserDetails) authentication.getPrincipal();
        var payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
        var courseRefId = payment.getCourseId();
        var courseDTO = productService.findCourseByReference(courseRefId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", paymentProperties.getPaystack_apiKey());
        headers.set("Content-Type", "application/json");

        var initializeTransactionHttpEntity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(VERIFY_TRANSACTION_URL+reference,HttpMethod.GET,initializeTransactionHttpEntity, VerifyTransactionResponse.class);
        if (response.getStatusCode().is2xxSuccessful()){
            var responseData = response.getBody();
            assert responseData != null;
            if (responseData.status.equals("success")) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment = paymentRepository.save(payment);

                var paymentEvent = new PaymentEvent(
                        payment.toPaymentDTO(),
                        EventType.PAYMENT_SUCCESSFUL,
                        null
                );
                applicationEventPublisher.publishEvent(paymentEvent);


                return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), Map.of("message", "Payment Success: Congratulations you can now access the course"));

            } else if (responseData.status.equals("failed")) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), Map.of("message", "Payment Failed: Please Try again"));
            } else {
                return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), Map.of("message", "Payment Pending: Please check again"));
            }

        } else {
            return new PaymentResponse(LocalDateTime.now().toString(), payment.getPaymentStatus(), Map.of("message", "Verification failed: Please try again"));
        }

    }

    @Builder
    private record InitializeTransaction(String amount, String email, String reference, String currency) {
    }

    private record VerifyTransactionAuthorization(
            String authorizationCode,
            String channel,
            String bank,
            String countryCode
    ){}
    private record VerifyTransactionResponse(
            String status,
            String message,
            VerifyTransactionData data
    ){}
    private record VerifyTransactionData(
            long id,
            String status,
            String reference,
            int amount,
            String receiptNumber,
            String currency,
            int requestedAmount,
            VerifyTransactionAuthorization authorization
    ){}
    private record InitializeTransactionResponse(
            String status,
            String message,
            InitializeTransactionData data
    ) {
    }

    private record InitializeTransactionData(
            String authorizationUrl,
            String accessCode,
            String reference
    ) {
    }
}
