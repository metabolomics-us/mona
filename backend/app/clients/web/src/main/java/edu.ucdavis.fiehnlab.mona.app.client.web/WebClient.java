package edu.ucdavis.fiehnlab.mona.app.client.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by wohlgemuth on 6/16/16.
 * used to provide the static content
 */
@SpringBootApplication
public class WebClient {

    /**
     * starts our web client
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(WebClient.class, args);
    }


}
