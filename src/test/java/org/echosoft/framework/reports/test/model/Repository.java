package org.echosoft.framework.reports.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Anton Sharapov
 */
@Entity
@Table(name = "repository")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @JsonProperty(value = "id", required = true)
    public long id;

    @Column(name = "name", nullable = false)
    @JsonProperty(value = "name", required = true)
    public String name;

    @Column(name = "full_name", unique = true, nullable = false)
    @JsonProperty(value = "full_name", required = true)
    public String fullName;

    @Column(name = "is_private", nullable = false)
    @JsonProperty(value = "private", required = true)
    public boolean isPrivate;

    @Column(name = "is_fork", nullable = false)
    @JsonProperty(value = "fork", required = true)
    public boolean isFork;

    @Column(name = "url", nullable = false)
    @JsonProperty(value = "html_url", required = true)
    public String url;

    @Column(name = "description", length = 2048)
    @JsonProperty(value = "description", required = true)
    public String description;

    @Column(name = "created_at", nullable = false)
    @JsonProperty(value = "created_at", required = true)
    public Date createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonProperty(value = "updated_at", required = true)
    public Date updatedAt;

    @Column(name = "pushed_at", nullable = false)
    @JsonProperty(value = "pushed_at", required = true)
    public Date pushedAt;

    @Column(name = "homepage")
    @JsonProperty(value = "homepage", required = false)
    public String homepage;

    @Column(name = "lang")
    @JsonProperty(value = "language", required = false)
    public String lang;

    @JsonProperty(value = "size", required = true)
    @Column(name = "size", nullable = false)
    public int size;

    @Column(name = "stars", nullable = false)
    @JsonProperty(value = "stargazers_count", required = true)
    public int stars;

    @Column(name = "forks", nullable = false)
    @JsonProperty(value = "forks", required = true)
    public int forks;

    @Column(name = "watchers", nullable = false)
    @JsonProperty(value = "watchers", required = true)
    public int watchers;

    @Column(name = "open_issues", nullable = false)
    @JsonProperty(value = "open_issues", required = true)
    public int openIssues;

    @Column(name = "archived", nullable = false)
    @JsonProperty(value = "archived", required = true)
    public boolean archived;

    @Column(name = "disabled", nullable = false)
    @JsonProperty(value = "disabled", required = true)
    public boolean disabled;

    @Column(name = "default_branch")
    @JsonProperty(value = "default_branch", required = true)
    public String defaultBranch;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonProperty(value = "owner", required = true)
    public Subject owner;

    @ManyToOne
    @JoinColumn(name = "license_id")
    @JsonProperty(value = "license", required = false)
    public License license;


    public boolean isActive() {
        return !archived;
    }

    public Subject.Type getOwnerType() {
        return owner.type;
    }

    public String getOwnerName() {
        return owner.login;
    }

    public String getLang() {
        return lang != null ? lang : "";
    }

    public int getStars() {
        return stars;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "[Repository{id:" + id + ", fullName:" + fullName + ", lang:" + lang + ", issues:" + openIssues + ", stars:" + stars + "}]";
    }
}
