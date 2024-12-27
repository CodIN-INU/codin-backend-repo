package inu.codin.codin.domain.user.exception;

public class UserPasswordChangeFailException extends RuntimeException{
    public UserPasswordChangeFailException(String message){
        super(message);
    }
}
