package genum.dataset.enums;

import lombok.Getter;

@Getter
public enum LicenseEnum {
    UNKNOWN("Unknown","", LicenseCategory.OTHER),
    DATABASE_CONTENTS("Database: Open Database, Contents: Database Contents", "https://opendatacommons.org/licenses/dbcl/1-0/", LicenseCategory.OPEN_DATA_COMMONS),
    ORIGINAL_AUTHORS("Database: Open Database, Contents: ©Original Authors", "https://opendatacommons.org/licenses/dbcl/1-0/", LicenseCategory.OPEN_DATA_COMMONS),
    OTHER("Other (specified in description","", LicenseCategory.OTHER),
    GPL_2("GPL 2", "https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html", LicenseCategory.GPL),
    APACHE_2_0("Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0", LicenseCategory.POPULAR),
    MIT("MIT","https://www.mit.edu/~amini/LICENSE.md", LicenseCategory.POPULAR),
    RAIL("RAIL (specified in description)", "https://www.licenses.ai/ai-licenses", LicenseCategory.SPECIAL),
    GPL_3("GPL 3", "https://www.gnu.org/licenses/gpl-3.0.html", LicenseCategory.GPL);


    private final String licenseName;
    private final String licenseUrl;
    private final LicenseCategory licenseCategory;

    LicenseEnum(String licenseName, String licenseUrl, LicenseCategory licenseCategory) {
        this.licenseName = licenseName;
        this.licenseUrl = licenseUrl;
        this.licenseCategory = licenseCategory;
    }
}
