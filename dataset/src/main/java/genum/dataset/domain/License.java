package genum.dataset.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import genum.dataset.enums.LicenseCategory;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record License(String licenseName, String licenseUrl, LicenseCategory licenseCategory) implements Serializable {

}
