package web.service;

/**
 * Exception thrown when a backend service is unavailable due to circuit breaker activation.
 * 
 * This allows the controller layer to distinguish between:
 * - Business errors (e.g., account not found: normal 404)
 * - Infrastructure errors (e.g., service down: circuit breaker fallback)
 * 
 * When this exception is thrown, it indicates the circuit breaker is OPEN and
 * the service is temporarily unavailable.
 */
public class ServiceUnavailableException extends RuntimeException {
    
    private final String serviceName;
    
    // Constructors
    public ServiceUnavailableException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }
    
    public ServiceUnavailableException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }
    
    // Getter
    public String getServiceName() {
        return serviceName;
    }
}
