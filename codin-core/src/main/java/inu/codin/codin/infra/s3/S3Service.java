package inu.codin.codin.infra.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    public S3Service(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }
    @Value("codin-s3-bucket")
    public String bucket;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_FILE_COUNT = 10; // 최대 파일 개수


    //모든 이미지 업로드
    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        validateFileCount(multipartFiles);
        List<String> uploadUrls = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            validateImageFileSize(multipartFile);
            validateImageFileExtension(multipartFile);
            uploadUrls.add(uploadFile(multipartFile));
        }
        return uploadUrls;

    }

    private void validateImageFileSize(MultipartFile multipartFile) {
        if (multipartFile.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 5MB를 초과할 수 없습니다.");
        }
    }

    private void validateFileCount(List<MultipartFile> multipartFiles) {
        if (multipartFiles.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("이미지 파일 개수는 최대 10개까지 업로드 가능합니다.");
        }
    }

    //각 이미지 S3에 업로드
    public String uploadFile(MultipartFile multipartFile) {
        validateImageFileExtension(multipartFile);

        String fileName = createFileName(multipartFile.getOriginalFilename());
        try{
            amazonS3Client.putObject(bucket, fileName, multipartFile.getInputStream(), null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    //중복 방지를 위해 이미지파일명 생성
    public String createFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension; // 고유한 파일명 생성

    }

    //파일 유효성 검사( 이미지 관련 확장자만 업로드 가능 설정)
    public void validateImageFileExtension(MultipartFile multipartFile) {
        List<String> validExtensions = List.of("jpg", "jpeg", "png", "gif");
        String extension = getExtension(multipartFile.getOriginalFilename());
        if (extension == null || !validExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("유효한 이미지 파일(jpg, jpeg, png, gif)만 업로드 가능합니다.");
        }
    }

    //확장자 추출
    private String getExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ""; // 확장자가 없는 경우
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }


    //삭제
    //삭제 안됐을경우 에러처리 필요
    public void deleteFile(String fileName) {
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalStateException("S3 버킷 이름이 설정되지 않았습니다.");
        }

        try {
            amazonS3Client.deleteObject(bucket, fileName);

            // 삭제가 제대로 되었는지 추가 검증
            if (amazonS3Client.doesObjectExist(bucket, fileName)) {
                throw new IllegalStateException("파일 삭제가 실패했습니다. 파일명: " + fileName);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 삭제 중 오류가 발생했습니다. 파일명: " + fileName, e);
        }
    }

    public void deleteFiles(List<String> fileUrls) {
        for (String fileUrl : fileUrls) {
            deleteFile(fileUrl);
        }
    }
}
