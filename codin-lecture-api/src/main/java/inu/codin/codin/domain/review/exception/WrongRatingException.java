package inu.codin.codin.domain.review.exception;

public class WrongRatingException extends RuntimeException{
    public WrongRatingException(String message){
        super(message);
    }
}
