package genum.data.serviceimplementation.payment;

import genum.data.payment.repository.ProductRepository;
import genum.data.serviceimplementation.payment.exception.ProductNotFoundException;
import genum.data.shared.security.CustomUserDetails;
import genum.data.payment.exceptions.PaymentUserAuthenticationFailed;
import genum.data.shared.payment.domain.PaymentResponse;
import genum.data.shared.payment.domain.ProductRequest;
import genum.data.payment.service.PaymentService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public final class PaystackPaymentService implements PaymentService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    public static final String PAYMENT_BASE_URL = "https://api.paystack.co";
    public static final String INITIALIZE_TRANSACTION_URL = PAYMENT_BASE_URL + "/transaction/initialize";
    public static final String VERIFY_TRANSACTION_URL = PAYMENT_BASE_URL + "transaction/verify/";
    public static final String LIST_TRANSACTIONS_URL = PAYMENT_BASE_URL + "/transaction";
    public static final String FETCH_TRANSACTION_URL = PAYMENT_BASE_URL + "/transaction/";
    @Override
    public PaymentResponse initializePayment(ProductRequest productRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String paymentId = "";
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PaymentUserAuthenticationFailed();
        }
        var user = (CustomUserDetails) authentication.getPrincipal();
        var course = productRepository.findById(productRequest.getProductId());
        if (course.isEmpty()){
            throw new ProductNotFoundException();
        }
        else {
            var amountInBaseUnits = course.get().getPrice() * 100;

            try {
                InitializeTransaction initializeTransaction = InitializeTransaction.builder()
                        .amount(String.valueOf(amountInBaseUnits))
                        .email(user.getEmail())
                        .reference(course.get().getReferenceId())
                        .currency("USD")
                        .build();
               var response =  restTemplate.postForObject()

            }
        }
    }

    @Builder
    private record InitializeTransaction(String amount, String email, String reference, String currency) {
    }
    private record InitializeTransactionResponse(String status, String message, InitializeTransactionData data){}
    private record InitializeTransactionData(String authorizationUrl, String accessCode, String reference){}



    @Override
    public PaymentResponse verifyPayment(String reference, String paymentId) {
        return null;
    }
}
