package genum.dataset.domain;

import genum.dataset.enums.TagCategory;
import genum.dataset.enums.TagsEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

//TODO: Add tags
public abstract class DatasetTags {
    @Getter
    public static final Set<Tag> tags = generateTags();

    private static Set<Tag> generateTags() {
        return Arrays.stream(TagsEnum.values())
                .map(tagsEnum -> new Tag(tagsEnum.getValue(), tagsEnum.getTagCategory()))
                .collect(Collectors.toSet());
    }

    private DatasetTags () {}


}
