package genum.learn.dto;

import genum.learn.enums.VideoUploadStatus;

public record ChunkedVideoUploadResponse(int chunkIndex, String uploadId, VideoUploadStatus uploadStatus) {
}
