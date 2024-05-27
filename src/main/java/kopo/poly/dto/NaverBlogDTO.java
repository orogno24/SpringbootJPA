package kopo.poly.dto;

import lombok.Builder;

@Builder
public record NaverBlogDTO(
        String title,
        String link,
        String description,
        String bloggername,
        String bloggerlink,
        String postdate
) {

}
