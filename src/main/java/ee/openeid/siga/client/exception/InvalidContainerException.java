package ee.openeid.siga.client.exception;

public class InvalidContainerException extends SigaDemoApiException {

    public InvalidContainerException(String message) {
        super("INVALID_CONTAINER_EXCEPTION", message);
    }
}
