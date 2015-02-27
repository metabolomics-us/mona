package filter

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * User: wohlgemuth
 * Date: 2/27/15
 * Time: 9:50 AM

 *
 * based on the work found here
 *
 *   https://github.com/davidtinker/grails-cors/blob/master/CorsGrailsPlugin.groovy
 */
class CorsFilter implements Filter {
    private final Map<String, String> optionsHeaders = new LinkedHashMap<String, String>();
    private Pattern allowOriginRegex;
    private String allowOrigin;
    private String exposeHeaders;

    public void init(FilterConfig cfg) throws ServletException {
        optionsHeaders.put("Access-Control-Allow-Headers", "origin, authorization, accept, content-type, x-requested-with, x-auth-token");
        optionsHeaders.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");
        optionsHeaders.put("Access-Control-Max-Age", "3600");
        for (Enumeration<String> i = cfg.getInitParameterNames(); i.hasMoreElements();) {
            String name = i.nextElement();
            if (name.startsWith("header:")) {
                optionsHeaders.put(name.substring(7), cfg.getInitParameter(name));
            }
        }
        String regex = cfg.getInitParameter("allow.origin.regex");
        if (regex != null) {
            allowOriginRegex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
        allowOrigin = optionsHeaders.remove("Access-Control-Allow-Origin");
        exposeHeaders = cfg.getInitParameter("expose.headers");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            if ("OPTIONS".equals(req.getMethod())) {
                if (checkOrigin(req, resp)) {
                    for (Map.Entry<String, String> e : optionsHeaders.entrySet()) {
                        resp.addHeader(e.getKey(), e.getValue());
                    }
// We need to return here since we don't want the chain to further process
// a preflight request since this can lead to unexpected processing of the preflighted
// request or a 405 - method not allowed in Grails 2.3
                    return;
                }
            } else if (checkOrigin(req, resp)) {
                if (exposeHeaders != null) {
                    resp.addHeader("Access-Control-Expose-Headers", exposeHeaders);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkOrigin(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin == null) {
//no origin; per W3C spec, terminate further processing for both preflight and actual requests
            return false;
        }
        boolean matches;
        if (allowOriginRegex != null) {
            matches = allowOriginRegex.matcher(origin).matches();
        } else {
            matches = allowOrigin == null || allowOrigin.equals("*") || allowOrigin.equals(origin);
        }
        if (matches) {
// if no 'Access-Control-Allow-Origin' specified in cors.headers then echo back Origin
            resp.addHeader("Access-Control-Allow-Origin", allowOrigin == null ? origin : allowOrigin);
            resp.addHeader("Access-Control-Allow-Credentials", "true");
            return true;
        } else {
            return false;
        }
    }

    public void destroy() {
    }
}