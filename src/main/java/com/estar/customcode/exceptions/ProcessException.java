package com.estar.customcode.exceptions;

import java.util.concurrent.CompletionException;

public class ProcessException extends CompletionException {
    public ProcessException(String message) {
        super(message);
    }
}
