package genum.shared.Sse;

public record ProgressEvent(String uploadId, int progress, String status) {
}
