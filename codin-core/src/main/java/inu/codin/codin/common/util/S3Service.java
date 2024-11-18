package inu.codin.codin.common.util;

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

    //모든 이미지 업로드
    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {
        List<String> uploadUrls = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            uploadUrls.add(uploadFile(multipartFile));
        }
        return uploadUrls;

    }

    //각 이미지 S3에 업로드
    public String uploadFile(MultipartFile multipartFile) {
        validateImageFile(multipartFile);

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
    public void validateImageFile(MultipartFile multipartFile) {
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
    public void deleteFile(String fileName) {
        amazonS3Client.deleteObject(bucket, fileName);
    }
}
