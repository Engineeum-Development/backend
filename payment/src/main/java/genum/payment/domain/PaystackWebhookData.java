package genum.payment.domain;

public record PaystackWebhookData(
        String status,
        String reference,
        String amount
        ) {
}
