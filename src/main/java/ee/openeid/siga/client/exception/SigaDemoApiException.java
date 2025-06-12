package ee.openeid.siga.client.exception;

import lombok.Getter;

@Getter
public class SigaDemoApiException extends RuntimeException {

    private final String errorCode;

    public SigaDemoApiException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }
}
