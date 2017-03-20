package edu.ucdavis.fiehnlab.mona.app.client.repository.controllers;

import edu.ucdavis.fiehnlab.mona.app.client.repository.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Arrays;

/**
 * Created by matthewmueller on 3/2/17.
 */

@RestController
public class RepositoryController {

    @Autowired
    RepositoryService repositoryService;

    @RequestMapping("/sync")
    public String hook(@RequestParam(value = "id") String id,
                       @RequestParam(value = "type") String event) throws IOException {
//
//        ProcessBuilder pb = new ProcessBuilder(
//                "curl",
//                "-s",
//                "http://mona.fiehnlab.ucdavis.edu/rest/spectra/"+id);
//
//        pb.redirectErrorStream(true);
//        Process p = pb.start();
//        InputStream is = p.getInputStream();
//
//        FileOutputStream outputStream = new FileOutputStream(
//                id+".txt");
//
//        String line;
//        BufferedInputStream bis = new BufferedInputStream(is);
//        byte[] bytes = new byte[100];
//        int numberByteReaded;
//        while ((numberByteReaded = bis.read(bytes, 0, 100)) != -1) {
//
//            outputStream.write(bytes, 0, numberByteReaded);
//            Arrays.fill(bytes, (byte) 0);
//
//        }
//
//        outputStream.flush();
//        outputStream.close();

        repositoryService.handleWebHook(id, event);

        return "Spectrum "+id+" : "+event;
    }
}
