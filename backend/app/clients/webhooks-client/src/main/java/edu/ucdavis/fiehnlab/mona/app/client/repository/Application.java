package edu.ucdavis.fiehnlab.mona.app.client.repository;

import edu.ucdavis.fiehnlab.mona.app.client.repository.service.RepositoryService;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Created by matthewmueller on 3/2/17.
 */
@SpringBootApplication
@Import({RestClientConfig.class})
public class Application implements ApplicationRunner {

    @Autowired
    private RepositoryService repositoryService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        if (applicationArguments.containsOption("username") && applicationArguments.containsOption("password")) {
            repositoryService.initializeWebHookRepository(applicationArguments.getOptionValues("username").get(0),
                    applicationArguments.getOptionValues("password").get(0));
        } else if (applicationArguments.containsOption("token")) {
            repositoryService.initializeWebHookRepository(applicationArguments.getOptionValues("token").get(0));
        } else {
            System.out.println();
            System.out.println("Usage: ");
            System.out.println("Supports token authentication as well as username/password login. For token based please use");
            System.out.println("\t --token=TOKEN\t\t\tyour previously generated token");
            System.out.println("");
            System.out.println("For username and password based authentication please use");
            System.out.println("\t --username=USER");
            System.out.println("\t --password=PASSWORD");

            repositoryService.shutdownApp(1);
        }
    }
}