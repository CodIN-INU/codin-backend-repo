package inu.codin.codin.infra.s3.exception;

public class S3BucketNameException extends RuntimeException{
    public S3BucketNameException(String message) {
        super(message);
    }
}
