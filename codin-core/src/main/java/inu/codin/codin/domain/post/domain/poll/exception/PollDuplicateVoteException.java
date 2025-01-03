package inu.codin.codin.domain.post.domain.poll.exception;

public class PollDuplicateVoteException extends RuntimeException {
    public PollDuplicateVoteException(String message) {
        super(message);
    }
}
