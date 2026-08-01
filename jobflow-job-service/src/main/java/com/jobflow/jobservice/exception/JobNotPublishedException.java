package com.jobflow.jobservice.exception;

public class JobNotPublishedException extends RuntimeException {
    public JobNotPublishedException(String message) {
        super(message);
    }
}
