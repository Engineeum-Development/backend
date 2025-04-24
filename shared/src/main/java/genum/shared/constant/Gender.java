package genum.shared.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");
    @JsonValue
    private final String value;
    Gender(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        for (Gender category : Gender.values()){
            if (category.getValue().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown Gender category: " + value);
    }
}
