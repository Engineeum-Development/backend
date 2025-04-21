package genum.learn.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ChunkedVideoUploadStatusMessage(String uploadId, String status, int videoChunk, String errorMessage ) {
}
