package genum.shared.payment.exception;

public class PaymentNotFoundException extends RuntimeException{
    public static final String MESSAGE = "Payment with that id was not found";

    public PaymentNotFoundException() {
        super(MESSAGE);
    }
}
