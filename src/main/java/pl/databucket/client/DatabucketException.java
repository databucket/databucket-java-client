package pl.databucket.client;

/**
 * An error returned by Databucket (HTTP status 4xx/5xx) or a failure to reach it.
 */
public class DatabucketException extends RuntimeException {

    private final int status;
    private final String body;

    public DatabucketException(int status, String message, String body) {
        super(status > 0 ? status + ": " + message : message);
        this.status = status;
        this.body = body;
    }

    public DatabucketException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
        this.body = null;
    }

    /** HTTP status code, or 0 when there was no response (connection error, timeout). */
    public int getStatus() {
        return status;
    }

    /** Raw response body, if any. */
    public String getBody() {
        return body;
    }
}
