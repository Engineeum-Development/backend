package genum.shared.product.exception;

public class ProductNotFoundException extends RuntimeException {

    private static final String MESSAGE = "This product no longer exists";

    public ProductNotFoundException() {
        super(MESSAGE);
    }
}
