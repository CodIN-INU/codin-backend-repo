package inu.codin.codin.domain.block.exception;

public class AlreadyBlockedException extends RuntimeException{
    public AlreadyBlockedException(String message) {
        super(message);
    }
}
