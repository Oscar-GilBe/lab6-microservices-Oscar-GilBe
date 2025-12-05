package web.service;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import web.model.Account;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service layer that encapsulates communication with the Accounts microservice.
 *
 * This class demonstrates service-to-service communication in
 * microservices architecture:
 * - Uses RestTemplate (configured with @LoadBalanced) to make HTTP calls
 * - Service discovery: The serviceUrl contains a logical name (e.g., "ACCOUNTS-SERVICE")
 *   that Eureka resolves to actual instance URLs
 * - Load balancing: When multiple instances exist, requests are automatically
 *   distributed across them
 * - Resilience: If one instance fails, Eureka routes requests to healthy instances
 * - Circuit Breaker: Prevents cascading failures by providing fallback responses
 *   when the Accounts service is unavailable
 *
 * This pattern hides the complexity of service discovery from the controller layer.
 *
 * @author Paul Chapman
 */
public class WebAccountsService {

    private final RestTemplate restTemplate;

    private final String serviceUrl;

    private final CircuitBreaker circuitBreaker;

    private final Logger logger = Logger.getLogger(WebAccountsService.class
            .getName());

    public WebAccountsService(String serviceUrl, RestTemplate restTemplate, 
                              CircuitBreakerFactory circuitBreakerFactory) {
        this.serviceUrl = serviceUrl.startsWith("http") ? serviceUrl
                : "http://" + serviceUrl;
        this.restTemplate = restTemplate;
        this.circuitBreaker = circuitBreakerFactory.create("accounts-service");
    }

    /**
     * Educational method demonstrating how RestTemplate uses Eureka for service discovery.
     *
     * The RestTemplate works because it uses a custom request-factory
     * that integrates with Spring Cloud LoadBalancer (replacing Ribbon in newer versions).
     * When you make a request to a service name like "ACCOUNTS-SERVICE":
     * 1. Spring Cloud intercepts the request
     * 2. Queries Eureka for available instances
     * 3. Selects an instance (load balancing)
     * 4. Replaces the service name with the actual URL
     * 5. Makes the HTTP request
     *
     * This method logs the request factory to show that it's not a standard RestTemplate.
     * This method exists purely for educational purposes to demonstrate the integration.
     */
    @PostConstruct
    public void demoOnly() {
        // Can't do this in the constructor because the RestTemplate injection
        // happens afterwards.
        logger.warning("The RestTemplate request factory is "
                + restTemplate.getRequestFactory());
    }

    /**
     * Finds an account by account number by calling the Accounts microservice.
     *
     * This method demonstrates service-to-service communication with circuit breaker:
     * - The serviceUrl (e.g., "http://ACCOUNTS-SERVICE") is resolved by Eureka
     * - RestTemplate automatically handles service discovery and load balancing
     * - Circuit breaker wraps the call to prevent cascading failures
     * - If the Accounts service is unavailable, throws ServiceUnavailableException
     *
     * @param accountNumber The 9-digit account number
     * @return The account if found, or null if not found (404)
     * @throws ServiceUnavailableException if the service is down (circuit breaker fallback)
     */
    public Account findByNumber(String accountNumber) {

        logger.info("findByNumber() invoked: for " + accountNumber);
        
        return circuitBreaker.run(
            () -> {
                try {
                    return restTemplate.getForObject(serviceUrl + "/accounts/{number}",
                            Account.class, accountNumber);
                } catch (HttpClientErrorException.NotFound e) {
                    // Account not found (404). This is a valid business case
                    logger.info("Account " + accountNumber + " not found (404)");
                    return null;
                }
            },
            // Fallback function. Called when circuit is OPEN or service fails
            throwable -> {
                logger.warning("Circuit breaker fallback for findByNumber(" + accountNumber + "): " + throwable.getMessage());
                throw new ServiceUnavailableException("ACCOUNTS-SERVICE", 
                    "Accounts service is temporarily unavailable. Please try again later.", 
                    throwable);
            }
        );
    }

    /**
     * Finds accounts by owner name (partial match) by calling the Accounts microservice.
     *
     * This method shows error handling in microservices with circuit breaker:
     * - Wraps the service call in a circuit breaker for resilience
     * - Catches HttpClientErrorException (404) when no accounts are found
     * - Throws ServiceUnavailableException if circuit breaker activates
     * - Demonstrates proper error differentiation for better UX
     *
     * @param name Partial owner name to search for
     * @return List of matching accounts, or null if none found (404)
     * @throws ServiceUnavailableException if the service is down (circuit breaker fallback)
     */
    public List<Account> byOwnerContains(String name) {
        logger.info("byOwnerContains() invoked:  for " + name);
        
        return circuitBreaker.run(
            () -> {
                Account[] accounts = null;
                try {
                    accounts = restTemplate.getForObject(serviceUrl
                            + "/accounts/owner/{name}", Account[].class, name);
                } catch (HttpClientErrorException.NotFound e) { // 404
                    // No accounts found (404). This is a valid business case
                    logger.info("No accounts found for owner: " + name);
                }

                if (accounts == null || accounts.length == 0) {
                    return null; // No accounts found
                } else {
                    return Arrays.asList(accounts);
                }
            },
            // Fallback function. Called when circuit is OPEN or service fails
            throwable -> {
                logger.warning("Circuit breaker fallback for byOwnerContains(" + name + "): " + throwable.getMessage());
                throw new ServiceUnavailableException("ACCOUNTS-SERVICE", 
                    "Accounts service is temporarily unavailable. Please try again later.", 
                    throwable);
            }
        );
    }
}
