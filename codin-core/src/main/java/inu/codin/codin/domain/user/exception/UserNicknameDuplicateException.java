package inu.codin.codin.domain.user.exception;

public class UserNicknameDuplicateException extends RuntimeException{
    public UserNicknameDuplicateException(String message){
        super(message);
    }
}
