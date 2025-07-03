package inu.codin.codin.domain.block.exception;

import inu.codin.codin.common.exception.GlobalException;
import lombok.Getter;

@Getter
public class BlockException extends GlobalException {

    private final BlockErrorCode errorCode;

    public BlockException(BlockErrorCode errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }
}
