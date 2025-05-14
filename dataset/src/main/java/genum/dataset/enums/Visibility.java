package genum.dataset.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Visibility {

    PUBLIC ("public"), PRIVATE("private");

    @JsonValue
    private final String value;

    Visibility(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Visibility fromValue(String value) throws IllegalArgumentException {
        for (Visibility visibility : Visibility.values()) {
            if (visibility.value.equalsIgnoreCase(value)) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("Unknown visibility category: " + value);
    }
    
}
