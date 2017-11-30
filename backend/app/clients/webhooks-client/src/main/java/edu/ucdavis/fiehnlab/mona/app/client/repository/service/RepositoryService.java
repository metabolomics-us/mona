package edu.ucdavis.fiehnlab.mona.app.client.repository.service;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by sajjan on 3/5/17.
 */
@Service
public class RepositoryService {

    @Autowired
    private MonaSpectrumRestClient monaSpectrumRestClient;

    @Autowired
    private LoginService loginService;

    @Autowired
    private ApplicationContext appContext;

    private Logger logger = Logger.getLogger(RepositoryService.class.toString());

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${mona.rest.server.monaHost:mona.fiehnlab.ucdavis.edu}")
    private String monaHost;

    @Value("${mona.rest.server.port:80}")
    private String monaPort;

    @Value("${server.host:localhost}")
    private String localHost;

    @Value("${server.port:8080}")
    private String localPort;


    private File getDownloadDirectory() {
        return new File("./webook_repository");
    }

    private void initializeDownloadDirectory() {
        try {
            File downloadDir = getDownloadDirectory();

            if (downloadDir.exists()) {
                if (!downloadDir.isDirectory()) {
                    throw new IOException("Download directory 'webhook_repository' is a file");
                }
            } else {
                downloadDir.mkdir();
            }
        } catch (IOException e) {
            e.printStackTrace();
            shutdownApp(1);
        }
    }

    private void createWebHook(String username, String token) {
        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", "Bearer " + token);
        header.setContentType(MediaType.APPLICATION_JSON);
        header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String monaUrl = "http://" + monaHost + ":" + monaPort + "/rest/webhooks";

        String localUrl = "http://" + localHost + ":" + localPort + "/sync";
        String webhookName = username + "_webhook-client";

        String requestJSON = "{\"name\": \"" + webhookName + "\", \"url\": \"" + localUrl + "\"}";
        HttpEntity<String> entity = new HttpEntity<String>(requestJSON, header);

        logger.info("Registering webhook at: " + monaUrl);
        restTemplate.postForObject(monaUrl, entity, String.class);
    }

    public void initializeWebHookRepository(String token) {
        String username = loginService.info(token).username();
        logger.info("Token verification successful");

        initializeDownloadDirectory();
        createWebHook(username, token);
    }

    public void initializeWebHookRepository(String username, String password) {
        String token = loginService.login(username, password).token();
        logger.info("Login successful");

        initializeDownloadDirectory();
        createWebHook(username, token);
    }

    public void handleWebHook(String id, String eventType) {
        try {
            if (eventType.equals("add") || Objects.equals(eventType, "update")) {
                logger.info("Saving spectrum with id: " + id);
                Spectrum spectrum = monaSpectrumRestClient.get(id);

                File spectrumFile = new File(getDownloadDirectory(), id + ".json");
                MonaMapper.create().writeValue(spectrumFile, spectrum);
                logger.info("Wrote spectrum " + id + " to: " + spectrumFile.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdownApp(int returnCode) {
        SpringApplication.exit(appContext, () -> returnCode);
    }
}
