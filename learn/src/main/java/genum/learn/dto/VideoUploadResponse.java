package genum.learn.dto;

import genum.learn.enums.VideoUploadStatus;

public record VideoUploadResponse(String videoId, String videoSeriesId, VideoUploadStatus videoUploadStatus) {
}
