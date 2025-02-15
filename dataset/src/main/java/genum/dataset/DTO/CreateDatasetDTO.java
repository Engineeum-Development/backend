package genum.dataset.DTO;

import genum.dataset.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDatasetDTO {
    private Visibility visibility;
    @NotBlank(message = "File Description is required")
    @Size(max = 2500, message = "Description too long")
    private String description;
    private List<String> tags;
}
