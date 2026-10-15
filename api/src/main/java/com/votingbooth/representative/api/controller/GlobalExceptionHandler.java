package com.votingbooth.representative.api.controller;

import com.votingbooth.representative.api.model.exception.RepresentativeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RepresentativeNotFoundException.class)
    public ProblemDetail handleNotFound(RepresentativeNotFoundException exc) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exc.getMessage());
    }

}
