package genum.dataset.DTO;

import genum.dataset.enums.Visibility;

import java.util.List;

public record DatasetUpdateRequest(
        String datasetId,
        String description,
        String visibility,
        List<String> tags
) {
}
