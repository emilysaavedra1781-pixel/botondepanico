package botondepanico.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(ConstraintViolationException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        String mensaje = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        String acceptHeader = request.getHeader("Accept");
        String uri = request.getRequestURI();
        
        if ((acceptHeader != null && acceptHeader.contains("application/json")) || 
            (uri != null && (uri.contains("/quick-add") || uri.contains("-json")))) {
            return ResponseEntity.badRequest().body(Map.of("error", mensaje));
        }
        
        redirectAttributes.addFlashAttribute("error", mensaje);
        
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}
