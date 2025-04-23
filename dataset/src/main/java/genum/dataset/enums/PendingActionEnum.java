package genum.dataset.enums;

import lombok.Getter;

@Getter
public enum PendingActionEnum {
    ADD_SUBTITLE("Add a subtitle", "Stand out on the listings page with a snappy subtitle"),
    ADD_TAGS("Add tags", "Make it easy for users to find your dataset in search"),
    ADD_DESCRIPTION("Add a description", "Share specifics about the context, sources and inspiration behind your dataset");


    private final String name;
    private final String description;

    PendingActionEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
