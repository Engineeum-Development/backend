package genum.payment.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

public class PaystackDomain {


    @Builder
    public record InitializeTransaction(String amount, String email, String reference, String currency) {
    }

    public record VerifyTransactionAuthorization(
            @JsonProperty("authorization_code")
            String authorizationCode,
            String channel,
            String bank,
            @JsonProperty("country_code")
            String countryCode
    ){}
    public record VerifyTransactionResponse(
            String status,
            String message,
            VerifyTransactionData data
    ){}
    public record VerifyTransactionData(
            long id,
            String status,
            String reference,
            int amount,
            @JsonProperty("receipt_number")
            String receiptNumber,
            String currency,
            @JsonProperty("requested_amount")
            int requestedAmount,
            VerifyTransactionAuthorization authorization
    ){}
    public record InitializeTransactionResponse(
            String status,
            String message,
            InitializeTransactionData data
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public record InitializeTransactionData(
            @JsonProperty("authorization_url")
            String authorizationUrl,
            @JsonProperty("access_code")
            String accessCode,
            String reference
    ) {
    }
}
