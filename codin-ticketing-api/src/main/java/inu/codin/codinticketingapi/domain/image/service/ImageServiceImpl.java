package inu.codin.codinticketingapi.domain.image.service;

import com.amazonaws.services.s3.AmazonS3Client;
import inu.codin.codinticketingapi.domain.image.exception.ImageErrorCode;
import inu.codin.codinticketingapi.domain.image.exception.ImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Primary
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucketName;

    private static final List<String> validExtensions = List.of("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_FILE_COUNT = 10; // 최대 파일 개수

    /** 다중 이미지 업로드 메소드 */
    @Override
    public List<String> handleImageUpload(List<MultipartFile> postImages) {
        if (postImages != null && !postImages.isEmpty()) {
            return uploadFiles(postImages);
        }
        return List.of();
    }

    /** 단일 이미지 업로드 메소드 */
    @Override
    public String handleImageUpload(MultipartFile postImage) {
        if (postImage != null && !postImage.isEmpty()) {
            validateImageFileSize(postImage);
            validateImageFileExtension(postImage);
            return uploadFile(postImage);
        }
        log.error("No image file provided");
        throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
    }

    /** 단일 이미지 삭제 */
    @Override
    public void deleteFile(String fileName) {
        if (bucketName == null || bucketName.isEmpty()) {
            throw new ImageException(ImageErrorCode.BAD_BUCKET_NAME);
        }

        try {
            amazonS3Client.deleteObject(bucketName, fileName);
            // 삭제가 제대로 되었는지 추가 검증
            if (amazonS3Client.doesObjectExist(bucketName, fileName)) {
                log.error("[deleteFile] Image is not existed, filaName : {}", fileName);
                throw new ImageException(ImageErrorCode.IMAGE_NOT_EXIST);
            }
        } catch (Exception e) {
            log.error("[deleteFile] Failed to delete image, filaName : {}", fileName);
            throw new ImageException(ImageErrorCode.REMOVE_FAILED);
        }
    }

    /** 이미지 파일 리스트 삭제 */
    @Override
    public void deleteFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls) {
            deleteFile(fileUrl);
        }
    }

    /** 모든 이미지 업로드 */
    private List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        validateFileCount(multipartFiles);
        List<String> uploadUrls = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            validateImageFileSize(multipartFile);
            validateImageFileExtension(multipartFile);
            uploadUrls.add(uploadFile(multipartFile));
        }
        return uploadUrls;
    }

    /** 이미지 S3에 업로드 */
    private String uploadFile(MultipartFile multipartFile) {
        validateImageFileExtension(multipartFile);

        String fileName = createFileName(multipartFile.getOriginalFilename());
        try{
            amazonS3Client.putObject(bucketName, fileName, multipartFile.getInputStream(), null);
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
        }
        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    /** 이미지 파일 크기 검증 */
    private void validateImageFileSize(MultipartFile multipartFile) {
        if (multipartFile.getSize() > MAX_FILE_SIZE) {
            throw new ImageException(ImageErrorCode.FILE_SIZE);
        }
    }

    /** 파일 개수 검증 */
    private void validateFileCount(List<MultipartFile> multipartFiles) {
        if (multipartFiles.size() > MAX_FILE_COUNT) {
            throw new ImageException(ImageErrorCode.MAX_FILE_COUNT);
        }
    }

    /** 파일 유효성 검사( 이미지 관련 확장자만 업로드 가능 설정) */
    private void validateImageFileExtension(MultipartFile multipartFile) {
        String extension = getExtension(multipartFile.getOriginalFilename());
        if (extension.isEmpty() || !validExtensions.contains(extension.toLowerCase())) {
            throw new ImageException(ImageErrorCode.BAD_IMAGE_TYPE);
        }
    }

    /** 확장자 추출 */
    private String getExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ""; // 확장자가 없는 경우
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /** 중복 방지를 위해 UUID 기반 이미지 파일명 생성 */
    private String createFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension; // 고유한 파일명 생성

    }
}
