package com.clova.anifriends.domain.image;

import java.util.List;

public record UploadImagesResponse(
    List<String> imageUrls
) {
    public static UploadImagesResponse of(List<String> imageUrls) {
        return new UploadImagesResponse(imageUrls);
    }
}
