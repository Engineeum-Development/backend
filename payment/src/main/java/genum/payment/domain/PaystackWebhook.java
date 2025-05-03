package genum.payment.domain;

public record PaystackWebhook(String event, PaystackWebhookData data) implements WebHook {
}
