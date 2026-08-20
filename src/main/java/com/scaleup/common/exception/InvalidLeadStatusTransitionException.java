package com.scaleup.common.exception;

public class InvalidLeadStatusTransitionException
        extends RuntimeException {

    public InvalidLeadStatusTransitionException(
            String message
    ) {
        super(message);
    }
}