package genum.dataset.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TagCategory {
    DATA_TYPE("Data Type"),
    SUBJECT("Subject"),
    GEOGRAPHY_AND_PLACES("Geography And Places"),
    AUDIENCE("Audience"),
    TECHNIQUE("Technique"),
    PACKAGES("Packages"),
    TASK("Task"),
    LANGUAGE("Language"),
    ARCHITECTURE("Architecture"),

    ;
    @JsonValue
    private final String value ;
    TagCategory(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TagCategory fromValue(String value) {
        for (TagCategory category : TagCategory.values()){
            if (category.getValue().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown tag category: " + value);
    }
}
