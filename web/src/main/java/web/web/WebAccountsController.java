package web.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import web.model.Account;
import web.model.SearchCriteria;
import web.service.ServiceUnavailableException;
import web.service.WebAccountsService;

import java.util.List;
import java.util.logging.Logger;

/**
 * MVC controller for the Web Service that acts as a client to the Accounts microservice.
 *
 * This controller demonstrates the client-side of microservices communication:
 * - Service discovery: Uses WebAccountsService which internally uses Eureka to find "ACCOUNTS-SERVICE"
 * - Load balancing: If multiple Accounts instances exist, requests are automatically distributed
 * - Circuit Breaker: Handles service failures gracefully with appropriate user messages as fallback responses
 * - Error differentiation: Distinguishes between "not found" (404) and "service unavailable" (circuit breaker)
 * - Separation of concerns: Business logic is in WebAccountsService, this handles HTTP/UI concerns
 *
 * This is a typical microservices pattern: a frontend service (this) calls backend services
 * (Accounts) without knowing their exact locations, relying on service discovery.
 *
 * @author Paul Chapman
 */
@Controller
public class WebAccountsController {

    private final WebAccountsService accountsService;

    private final Logger logger = Logger.getLogger(WebAccountsController.class
            .getName());

    @Autowired
    public WebAccountsController(WebAccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields("accountNumber", "searchText");
    }

    @RequestMapping("/accounts")
    public String goHome() {
        return "index";
    }

    @RequestMapping("/accounts/{accountNumber}")
    public String byNumber(Model model,
                           @PathVariable("accountNumber") String accountNumber) {

        logger.info("web-service byNumber() invoked: " + accountNumber);

        Account account = accountsService.findByNumber(accountNumber);
        logger.info("web-service byNumber() found: " + account);
        
        if (account == null) {
            // Account not found (404 from service). Normal business case
            model.addAttribute("message", "Account " + accountNumber + " not found.");
            return "account";
        }
        
        model.addAttribute("account", account);
        return "account";
    }

    @RequestMapping("/accounts/owner/{text}")
    public String ownerSearch(Model model, @PathVariable("text") String name) {
        logger.info("web-service byOwner() invoked: " + name);

        List<Account> accounts = accountsService.byOwnerContains(name);
        logger.info("web-service byOwner() found: " + accounts);
        model.addAttribute("search", name);
        if (accounts != null && !accounts.isEmpty()) {
            model.addAttribute("accounts", accounts);
        } else {
            // No accounts found (404 from service) . Normal business case
            model.addAttribute("message", "No accounts found for owner: " + name);
        }
        return "accounts";
    }

    /**
     * Exception handler for ServiceUnavailableException.
     * This handles cases where the circuit breaker is OPEN and the service is down.
     * 
     * Differentiates between:
     * - Business errors (404, handled in controller methods)
     * - Infrastructure errors (service down, handled here)
     * 
     * @param ex The ServiceUnavailableException thrown by the circuit breaker fallback
     * @param model The Spring MVC model
     * @return The error view name
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public String handleServiceUnavailable(ServiceUnavailableException ex, Model model) {
        logger.warning("Service unavailable: " + ex.getMessage());
        
        model.addAttribute("serviceName", ex.getServiceName());
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("errorType", "SERVICE_UNAVAILABLE");
        model.addAttribute("suggestion", "The service is temporarily down. Please try again in a few moments.");
        
        return "error";
    }

    @RequestMapping(value = "/accounts/search", method = RequestMethod.GET)
    public String searchForm(Model model) {
        model.addAttribute("searchCriteria", new SearchCriteria());
        return "accountSearch";
    }

    @RequestMapping(value = "/accounts/dosearch")
    public String doSearch(Model model, SearchCriteria criteria,
                           BindingResult result) {
        logger.info("web-service search() invoked: " + criteria);

        criteria.validate(result);

        if (result.hasErrors()) {
            return "accountSearch";
        }

        String accountNumber = criteria.getAccountNumber();
        if (StringUtils.hasText(accountNumber)) {
            return byNumber(model, accountNumber);
        } else {
            String searchText = criteria.getSearchText();
            return ownerSearch(model, searchText);
        }
    }
}
