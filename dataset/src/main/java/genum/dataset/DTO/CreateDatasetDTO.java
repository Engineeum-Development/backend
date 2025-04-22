package genum.dataset.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateDatasetDTO(
        @NotBlank(message = "Must not be blank") String datasetName,
        String visibility,
        @NotBlank(message = "File Description is required") String description,
        @Size(max = 2500, message = "Description too long")
        List<String> tags) implements Serializable {
}
