package genum.shared.exception;

public class UploadSizeLimitExceededException extends RuntimeException {

    public static final String MESSAGE = "File size %s exceeds maximum allowed size of %s";

    public UploadSizeLimitExceededException(long uploadSize, long maxSize) {
        super(MESSAGE.formatted(uploadSize, maxSize));
    }
}
