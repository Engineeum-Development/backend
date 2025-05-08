package genum.shared.upload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record CloudinaryResponse(
        @JsonProperty("asset_id") String assetId,
        @JsonProperty("public_id") String publicId,
        String format,
        @JsonProperty("resource_type") String resourceType,
        String type,
        String url,
        @JsonProperty("secure_url") String secureUrl
) {
}
