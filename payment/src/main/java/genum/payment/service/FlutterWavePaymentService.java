package genum.payment.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import genum.payment.config.PaymentProperties;
import genum.payment.domain.WebHook;
import genum.payment.repository.PaymentRepository;
import genum.course.service.CourseService;
import genum.shared.payment.domain.PaymentResponse;
import genum.shared.payment.domain.ProductRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FlutterWavePaymentService implements PaymentService {

    public static final String FLUTTERWAVE_BASE_API = "https://api.flutterwave.com/v3/";
    public static final String INITIALIZE_TRANSACTION_ENDPOINT = FLUTTERWAVE_BASE_API + "payments";
    public static final String VERIFY_TRANSACTION_ENDPOINT = FLUTTERWAVE_BASE_API + "transactions/" + "%s" + "/verify";
    private final PaymentProperties paymentProperties;
    private final CourseService courseService;
    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /*
    ReferenceId for each transaction consists of the concatenation of the courseId
     and the user email delimited by ":"
     */
    @Override
    public PaymentResponse initializePayment(ProductRequest productRequest) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new PaymentUserAuthenticationFailed();
//        }
//        var user = (CustomUserDetails) authentication.getPrincipal();
//        CourseDTO course = productService.findCourseByReference(productRequest.productId());
//        var payment = CoursePayment.builder()
//                .courseId(course.referenceId())
//                .paymentInitializationDate(LocalDateTime.now().toString())
//                .paymentValue(course.price())
//                .userid(user.getEmail())
//                .paymentPlatForm(PaymentPlatform.FLUTTER_WAVE)
//                .paymentStatus(PaymentStatus.PENDING)
//                .build();
//        payment = paymentRepository.save(payment);
//
//        InitializeTransaction initializeTransaction = InitializeTransaction.builder()
//                .amount(String.valueOf(course.price()))
//                .customer(new Customer(user.getEmail()))
//                .redirectUrl(paymentProperties.getFlutterWave_CallBackUrl())
//                .currency("NGN")
//                .reference(course.referenceId() + ":" + user.getEmail() + ":" + payment.getId())
//                .build();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + paymentProperties.getFlutterWave_SecretKey());
//        headers.set("Content-Type", "application/json");
//
//        HttpEntity<InitializeTransaction> initializeTransactionFlutterWaveHttpEntity = new HttpEntity<>(initializeTransaction, headers);
//        var response = restTemplate.exchange(INITIALIZE_TRANSACTION_ENDPOINT,
//                HttpMethod.POST,
//                initializeTransactionFlutterWaveHttpEntity,
//                InitializeTransactionResponse.class);
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            var responseBody = response.getBody();
//            assert responseBody != null;
//
//            if (responseBody.status.equals("success")) {
//                PaymentEvent paymentEvent = new PaymentEvent(payment.toPaymentDTO(),
//                        genum.payment.event.EventType.PAYMENT_SUCCESSFUL, null);
//                applicationEventPublisher.publishEvent(paymentEvent);
//                return new PaymentResponse(LocalDateTime.now().toString(), PaymentStatus.PENDING, Map.of(
//                        "message", "Payment initialization was a success",
//                        "authorization_url", response.getBody().data().link()));
//            } else {
//                payment.setPaymentStatus(PaymentStatus.FAILED);
//                paymentRepository.save(payment);
//                return new PaymentResponse(LocalDateTime.now().toString(), PaymentStatus.FAILED, Map.of(
//                        "message", "Payment initialization failed",
//                        "status", "failed"
//                ));
//            }
//
//        } else {
//            payment.setPaymentStatus(PaymentStatus.FAILED);
//            paymentRepository.save(payment);
//            return new PaymentResponse(LocalDateTime.now().toString(), PaymentStatus.FAILED, Map.of(
//                    "message", "Payment initialization failed",
//                    "status", "failed"
//            ));
//        }
        return null;
    }


    @Override
    public PaymentResponse verifyPayment(String reference) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer" +  paymentProperties.getFlutterWave_SecretKey());
//        headers.set("Content-Type", "application/json");
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new PaymentUserAuthenticationFailed();
//        }
//        var user = (CustomUserDetails) authentication.getPrincipal();
//
//        String appPaymentId = reference.split(":")[2];
//        String userEmail = reference.split(":")[1];
//        var courseReference = reference.split(":")[0];
//
//        var courseDTO = productService.findCourseByReference(courseReference);
//
//        var optionalPayment = paymentRepository.findById(appPaymentId);
//
//
//        if (!user.getEmail().equals(userEmail) || optionalPayment.isEmpty()) {
//            return new PaymentResponse(LocalDateTime.now().toString(),
//                    PaymentStatus.FAILED,
//                    Map.of("message", "This payment does not exist or user not authenticated",
//                            "status", "failed"));
//        }
//
//        var verifyTransactionHttpEntity = new HttpEntity<>(headers);
//
//        var response = restTemplate
//                .exchange(VERIFY_TRANSACTION_ENDPOINT.formatted(paymentId),
//                        HttpMethod.GET,
//                        verifyTransactionHttpEntity,
//                        VerifyTransactionResponse.class);
//        if (response.getStatusCode().is2xxSuccessful()) {
//            var payment = optionalPayment.get();
//            var responseData = response.getBody();
//            assert responseData != null;
//            if (responseData.status.equals("success")) {
//                payment.setPaymentStatus(PaymentStatus.COMPLETED);
//                paymentRepository.save(payment);
//                var courseEvent = new ProductEvent(
//                        courseDTO,
//                        EventType.COURSE_ENROLLED,
//                        LocalDateTime.now(),
//                        null
//                );
//                applicationEventPublisher.publishEvent(courseEvent);
//                return new PaymentResponse(
//                        LocalDateTime.now().toString(),
//                        PaymentStatus.COMPLETED,
//                        Map.of("message", "The payment was a success",
//                                "status", "success"));
//            } else {
//                payment.setPaymentStatus(PaymentStatus.FAILED);
//                paymentRepository.save(payment);
//                return new PaymentResponse(
//                        LocalDateTime.now().toString(),
//                        PaymentStatus.FAILED,
//                        Map.of("message", "The payment failed, please try again later",
//                                "status", "failed"));
//            }
//        } else {
//            return new PaymentResponse(
//                    LocalDateTime.now().toString(),
//                    PaymentStatus.PENDING,
//                    Map.of("message", "The Payment couldn't be verified,please try again",
//                            "status", "failed"));
//        }
        return null;
    }

    @Override
    public boolean handleWebHook(WebHook webHook) {
        return false;
    }


    @Builder
    private record InitializeTransaction(

            @JsonProperty(value = "tx_ref")
            String reference,
            String amount,
            String currency,
            @JsonProperty(value = "redirect_url")
            String redirectUrl,
            Customer customer) {
    }

    private record Customer(String email) {
        Customer {
            assert Objects.nonNull(email) && !email.isBlank();
        }
    }

    private record InitializeTransactionResponse(String status, String message,
                                                 InitializeTransactionResponseData data) {
    }

    private record InitializeTransactionResponseData(String link) {
    }

    private record VerifyTransactionResponse(
            String status,
            String message,
            VerifyTransactionResponseData data
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VerifyTransactionResponseData(
            int id,
            @JsonProperty(value = "tx_ref")
            String reference,
            int amount,
            String currency,
            int chargedAmount,
            String status,
            Customer customer
    ) {
    }
}
