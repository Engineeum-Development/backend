package genum.dataset.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import genum.dataset.enums.GeoSpatialCoverage;

import java.time.LocalDate;
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record Coverage(LocalDate coverageStartDate, LocalDate coverageEndDate, GeoSpatialCoverage geoSpatialCoverage ) {
}
