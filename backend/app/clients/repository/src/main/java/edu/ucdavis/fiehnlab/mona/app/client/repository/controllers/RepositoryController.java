package edu.ucdavis.fiehnlab.mona.app.client.repository.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by matthewmueller on 3/2/17.
 */

@RestController
public class RepositoryController {

    @RequestMapping("/webhook")
    public String hook(@RequestParam(value="id") String id,
                       @RequestParam(value="type") String event){
        return "Spectrum "+id+" : "+event;
    }
}
