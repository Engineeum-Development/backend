package genum.learn.domain;

public record VideoChunkMetadata(String uploadId, int chunkIndex, int totalChunks, long totalFileSize, String originalFileName) {
}
