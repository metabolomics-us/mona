package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.domain;


import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "webhook")
@Profile({"mona.persistence"})
public class WebHook implements Serializable {
    @Id
    private String name;
    private String url;
    private String description = "None provided";
    private String emailAddress;

    public WebHook(String name, String url, String description, String emailAddress) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.emailAddress = emailAddress;
    }

    public WebHook() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setUsername(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebHook webHook = (WebHook) o;
        return Objects.equals(name, webHook.name) && Objects.equals(url, webHook.url) && Objects.equals(description, webHook.description) && Objects.equals(emailAddress, webHook.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, description, emailAddress);
    }
}
