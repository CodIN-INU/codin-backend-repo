package inu.codin.codin.domain.block.exception;

public class SelfBlockedException extends RuntimeException{
    public SelfBlockedException(String message){
        super(message);
    }
}
