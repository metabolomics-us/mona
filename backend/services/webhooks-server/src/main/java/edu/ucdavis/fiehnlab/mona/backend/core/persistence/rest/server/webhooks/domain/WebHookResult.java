package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.domain;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "webhooks_triggered")
@Profile({"mona.persistence"})
public class WebHookResult implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    private UUID id;
    private String name;
    private String url;
    private Boolean success = true;
    private String error = "";
    private Date invoked = new Date();

    public WebHookResult() {
    }

    public WebHookResult(String name, String url, Boolean success, String error, Date invoked) {
        this.name = name;
        this.url = url;
        this.success = success;
        this.error = error;
        this.invoked = invoked;
    }

    public WebHookResult(String name, String url, Boolean success, String error) {
        this.name = name;
        this.url = url;
        this.success = success;
        this.error = error;
    }

    public WebHookResult(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Date getInvoked() {
        return invoked;
    }

    public void setInvoked(Date invoked) {
        this.invoked = invoked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebHookResult that = (WebHookResult) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(url, that.url) && Objects.equals(success, that.success) && Objects.equals(error, that.error) && Objects.equals(invoked, that.invoked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, url, success, error, invoked);
    }
}
