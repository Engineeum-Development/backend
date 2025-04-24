package genum.dataset.domain;

import genum.dataset.enums.LicenseEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Licenses {

    @Getter
    public static final Set<License> licenses = generateLicenses();

    private static Set<License> generateLicenses() {
        return Arrays.stream(LicenseEnum.values())
                .map(licenseEnum -> new License(licenseEnum.getLicenseName(),licenseEnum.getLicenseUrl(),licenseEnum.getLicenseCategory()))
                .collect(Collectors.toSet());
    }

    private Licenses() {}
}
