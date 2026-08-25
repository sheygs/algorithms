package CircuitBreaker;

/**
* Simple response object.
*/
public class Response {
    int status;
    String body = "body";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}