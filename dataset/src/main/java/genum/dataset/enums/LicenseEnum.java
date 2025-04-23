package genum.dataset.enums;

import lombok.Getter;

@Getter
public enum LicenseEnum {

    GNU_LICENCE("GNU License", "https://gnulicense.org/license");
    private final String licenseName;
    private final String licenseUrl;

    LicenseEnum(String licenseName, String licenseUrl) {
        this.licenseName = licenseName;
        this.licenseUrl = licenseUrl;
    }
}
