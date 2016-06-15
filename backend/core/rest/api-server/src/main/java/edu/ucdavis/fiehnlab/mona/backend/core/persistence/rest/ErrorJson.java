package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest;

import java.util.Map;

/**
 * simple error object for status messages from the rest server
 */
public class ErrorJson {

    public Integer status;
    public String error;
    public String message;
    public String timeStamp;
    public String trace;
    public String uri;
    public String url;

    public ErrorJson(int status, Map<String, Object> errorAttributes) {
        this.status = status;
        this.error = (String) errorAttributes.get("error");
        this.message = (String) errorAttributes.get("message");
        this.timeStamp = errorAttributes.get("timestamp").toString();
        this.trace = (String) errorAttributes.get("trace");
        this.uri = errorAttributes.get("uri").toString();
        this.url = errorAttributes.get("url").toString();
    }

}
