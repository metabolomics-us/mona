package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by wohlgemuth on 6/14/16.
 */
@RestController
public class CustomErrorController implements ErrorController {

    private static final String PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = PATH)
    ErrorJson error(HttpServletRequest request, HttpServletResponse response) {
        // Appropriate HTTP response code (e.g. 404 or 500) is automatically set by Spring.
        // Here we just define response body.
        return new ErrorJson(response.getStatus(), getErrorAttributes(request, true));
    }

    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {

        WebRequest webRequest = new ServletWebRequest(request);
        ErrorAttributeOptions errorAttributeOptions;
        if(includeStackTrace) {
            errorAttributeOptions = ErrorAttributeOptions.defaults().including(ErrorAttributeOptions.Include.STACK_TRACE);
        } else {
            errorAttributeOptions = ErrorAttributeOptions.defaults();
        }
        Map<String, Object> map = errorAttributes.getErrorAttributes(webRequest, errorAttributeOptions);
        map.put("uri", request.getRequestURI());
        map.put("url", request.getRequestURL().toString());
        return map;
    }
}
