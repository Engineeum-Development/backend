package genum.dataset.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;

@Getter
public enum DatasetType {
    JSON ("JSON"), CSV("CSV");

    @JsonValue
    private final String value;

    DatasetType(String value) {
        this.value = value;
    }

    @JsonCreator
    public DatasetType fromValue(String value) {
        if (Objects.nonNull(value)) {
            for (DatasetType datasetType : DatasetType.values()) {
                if (datasetType.value.equalsIgnoreCase(value)){
                    return datasetType;
                }
            }
            throw new IllegalArgumentException("%s is not a valid datasetType".formatted(value));
        }
        throw new IllegalArgumentException("Value cannot be null");
    }
}
