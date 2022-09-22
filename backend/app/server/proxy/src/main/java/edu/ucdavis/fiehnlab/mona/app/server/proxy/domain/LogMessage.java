package edu.ucdavis.fiehnlab.mona.app.server.proxy.domain;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "logs")
@Profile({"mona.persistence"})
public class LogMessage implements Serializable {
  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "id", columnDefinition = "VARCHAR(255)")
  private UUID id;
  private Integer httpStatus;
  private String httpMethod;
  private String path;
  private String queryString;
  private String postData;
  private String clientCountry;
  private String clientRegion;
  private String clientCity;
  private Long duration;
  private Date date;

  public LogMessage(Integer httpStatus, String httpMethod, String path, String queryString, String postData, String clientCountry, String clientRegion, String clientCity, Long duration, Date date) {
    this.httpStatus = httpStatus;
    this.httpMethod = httpMethod;
    this.path = path;
    this.queryString = queryString;
    this.postData = postData;
    this.clientCountry = clientCountry;
    this.clientRegion = clientRegion;
    this.clientCity = clientCity;
    this.duration = duration;
    this.date = date;
  }

  public LogMessage() {
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Integer getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(Integer httpStatus) {
    this.httpStatus = httpStatus;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getQueryString() {
    return queryString;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  public String getPostData() {
    return postData;
  }

  public void setPostData(String postData) {
    this.postData = postData;
  }

  public String getClientCountry() {
    return clientCountry;
  }

  public void setClientCountry(String clientCountry) {
    this.clientCountry = clientCountry;
  }

  public String getClientRegion() {
    return clientRegion;
  }

  public void setClientRegion(String clientRegion) {
    this.clientRegion = clientRegion;
  }

  public String getClientCity() {
    return clientCity;
  }

  public void setClientCity(String clientCity) {
    this.clientCity = clientCity;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LogMessage that = (LogMessage) o;
    return Objects.equals(id, that.id) && Objects.equals(httpStatus, that.httpStatus) && Objects.equals(httpMethod, that.httpMethod) && Objects.equals(path, that.path) && Objects.equals(queryString, that.queryString) && Objects.equals(postData, that.postData) && Objects.equals(clientCountry, that.clientCountry) && Objects.equals(clientRegion, that.clientRegion) && Objects.equals(clientCity, that.clientCity) && Objects.equals(duration, that.duration) && Objects.equals(date, that.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, httpStatus, httpMethod, path, queryString, postData, clientCountry, clientRegion, clientCity, duration, date);
  }
}
