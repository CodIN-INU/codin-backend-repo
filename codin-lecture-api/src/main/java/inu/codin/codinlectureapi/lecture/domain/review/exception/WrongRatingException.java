package inu.codin.codinlectureapi.lecture.domain.review.exception;

public class WrongRatingException extends RuntimeException{
    public WrongRatingException(String message){
        super(message);
    }
}
