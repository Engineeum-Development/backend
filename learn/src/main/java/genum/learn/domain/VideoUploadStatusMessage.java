package genum.learn.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record VideoUploadStatusMessage(String uploadId, String status, int progress, String downloadUrl, String errorMessage) {
}
