package genum.dataset.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum GeoSpatialCoverage {
    CITY("City"), COUNTRY("Country"), WORLDWIDE("Worldwide");
    @JsonValue
    private final String value;
    GeoSpatialCoverage(String value) {
        this.value = value;
    }

    @JsonCreator
    public static GeoSpatialCoverage fromValue(String value) {
        for (GeoSpatialCoverage category: GeoSpatialCoverage.values()){
            if (category.getValue().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown geqSpatial coverage: " + value);
    }
}
