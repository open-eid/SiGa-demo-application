package ee.openeid.siga.client.controller;

import ee.openeid.siga.client.exception.SigaDemoApiException;
import ee.openeid.siga.webapp.json.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SigaDemoApiException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse genericSigaApiException(SigaDemoApiException exception) {
        log.error("Siga-Demo API exception - {}", exception);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(exception.getErrorCode());
        errorResponse.setErrorMessage(exception.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse genericException(Exception exception) {
        log.error("Internal server error - {}", exception.getLocalizedMessage(), exception);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(exception.getMessage());
        errorResponse.setErrorCode("INTERNAL_SERVER_ERROR");
        return errorResponse;
    }
}
