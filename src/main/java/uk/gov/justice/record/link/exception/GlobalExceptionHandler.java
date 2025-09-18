package uk.gov.justice.record.link.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({ResponseStatusException.class, Exception.class})
    public String handleException(Exception ex, Model model, HttpServletRequest request) {
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rsEx = (ResponseStatusException) ex;
            log.warn("ResponseStatusException caught: {} - {} for path: {}", 
                    rsEx.getStatusCode(), rsEx.getReason(), request.getRequestURI());
            model.addAttribute("statusCode", rsEx.getStatusCode().value());
        } else {
            log.error("Unexpected exception caught for path: {}", request.getRequestURI(), ex);
        }
        
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        return "error/generic-error";
    }
}
