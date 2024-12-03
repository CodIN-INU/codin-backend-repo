package inu.codin.codin.infra.s3.exception;

public class ImageCountException extends RuntimeException{
    public ImageCountException(String message) {
        super(message);
    }
}
