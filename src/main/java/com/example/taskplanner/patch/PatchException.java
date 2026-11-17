package com.example.taskplanner.patch;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class PatchException extends ResponseStatusException {

    public PatchException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public static PatchException badRequest(String reason) {
        return new PatchException(HttpStatus.BAD_REQUEST, reason);
    }

    public static PatchException conflict(String reason) {
        return new PatchException(HttpStatus.CONFLICT, reason);
    }

    public static PatchException unprocessable(String reason) {
        return new PatchException(HttpStatus.UNPROCESSABLE_CONTENT, reason);
    }
}
