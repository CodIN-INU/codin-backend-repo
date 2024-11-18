package inu.codin.codin.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PostContentUpdateReqDTO {

    @Schema(description = "게시물 제목", example = "Updated Title")
    @NotBlank
    private String title;

    @Schema(description = "게시물 내용", example = "Updated content")
    @NotBlank
    private String content;

    //이미지 별도 Multipart (RequestPart 사용)

}