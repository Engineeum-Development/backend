package genum.shared.learn.exception;

public class VideoUploadException extends RuntimeException{

    public static final String MESSAGE = "Video couldn't be uploaded -> %s";


    public VideoUploadException(String reason) {
        super(MESSAGE.formatted(reason));
    }
}
