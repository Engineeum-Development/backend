package genum.dataset.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum LicenseCategory {
    CREATIVE_COMMONS("Creative Commons"),
    GPL("GPL"),
    OPEN_DATA_COMMONS("Open Data Commons"),
    COMMUNITY_DATA_LICENSE("Community Data License"),
    SPECIAL("Special"),
    RESPONSIBLE_AI_LICENSE("Responsible AI License"),
    OTHER("Other"),
    POPULAR("Popular")
    ;
    @JsonValue
    private final String value;

    @JsonCreator
    public static LicenseCategory fromValue(String value) {
        for (LicenseCategory category: LicenseCategory.values()){
            if (category.getValue().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown license category: " + value);
    }

    LicenseCategory(String value) {
        this.value = value;
    }
}
