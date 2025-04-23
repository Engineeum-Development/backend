package genum.dataset.domain;

import java.util.Set;
import java.util.stream.Collectors;

//TODO: Add tags
public abstract class DatasetTags {

    public static final Set<Tag> tags = Set.of();

    private DatasetTags () {}

    public static Set<Tag> getTags() {
        return DatasetTags.tags;
    }
    public static Set<Tag> getTagsWithCategory(TagCategory tagCategory) {
        return tags
                .stream()
                .filter( tag -> tag.tagCategory().equals(tagCategory))
                .collect(Collectors.toSet());
    }
}
