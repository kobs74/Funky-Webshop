package com.keyscout24.error;

import java.util.Map;

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/** Port of app/error: shows templates/errors/403.html, 404.html and 500.html. */
@Component
public class ErrorController implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        return switch (status.value()) {
            case 403, 404 -> new ModelAndView("errors/" + status.value(), model, status);
            default -> status.is5xxServerError() ? new ModelAndView("errors/500", model, status) : null;
        };
    }
}
