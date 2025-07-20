package inu.codin.codinlectureapi.lecture.domain.review.exception;

public class ReviewExistenceException extends RuntimeException{
    public ReviewExistenceException(String message){
        super(message);
    }
}
