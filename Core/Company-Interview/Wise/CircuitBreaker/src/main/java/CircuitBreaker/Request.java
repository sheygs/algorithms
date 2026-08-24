package CircuitBreaker;

/**
* Simple request object.
*/
public class Request {
   private final String service;

   public Request(String service) {
        this.service = service;
   }

   public String getService() {
        return service;
   }
}
