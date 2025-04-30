package genum.dataset.enums;

import lombok.Getter;

@Getter
public enum GeoSpatialCoverage {
    CITY("City"), COUNTRY("Country"), WORLDWIDE("Worldwide");
    private final String value;
    GeoSpatialCoverage(String value) {
        this.value = value;
    }
}
