package inu.codin.codinticketingapi.domain.s3.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    List<String> handleImageUpload(List<MultipartFile> postImages);
    public String handleImageUpload(MultipartFile postImage);
    void deleteFiles(List<String> fileUrls);
    void deleteFile(String fileName);
}
