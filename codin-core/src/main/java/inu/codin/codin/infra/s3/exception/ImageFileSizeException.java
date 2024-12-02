package inu.codin.codin.infra.s3.exception;

public class ImageFileSizeException extends RuntimeException{
    public ImageFileSizeException(String message) {
        super(message);
    }
}
