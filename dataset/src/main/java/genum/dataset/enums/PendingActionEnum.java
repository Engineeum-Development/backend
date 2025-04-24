package genum.dataset.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PendingActionEnum {
    ADD_SUBTITLE("Add a subtitle", "Stand out on the listings page with a snappy subtitle"),
    ADD_TAGS("Add tags", "Make it easy for users to find your dataset in search"),
    ADD_DESCRIPTION("Add a description", "Share specifics about the context, sources and inspiration behind your dataset"),
    UPLOAD_AN_IMAGE("Upload an image","Make your dataset pop with an eye-catching cover image and thumbnail"),
    ADD_FILE_INFORMATION("Add file information", "Help others navigate your dataset with a description of each file"),
    SPECIFY_A_LICENSE("Specify a license", "Help other users understand how they can work with and share the data"),
    SPECIFY_PROVENANCE("Specify provenance", "Let others know how the data was collected and organized in the metadata tab"),
    SPECIFY_UPDATE_FREQUENCY("Specify update frequency", "Let others know if the dataset will be regularly updated in the metadata tab"),
    MAKE_PUBLIC("Make public", "Make your dataset available to the Kaggle community"),
    PUBLISH_A_NOTEBOOK("Publish a notebook", "Provide an example of the data in use so other users can get started quickly");



    private final String name;
    private final String description;

    PendingActionEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
