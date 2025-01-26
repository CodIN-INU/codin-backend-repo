package inu.codin.codin.domain.lecture.domain.review.exception;

public class WrongRatingException extends RuntimeException{
    public WrongRatingException(String message){
        super(message);
    }
}
