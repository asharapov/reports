package org.echosoft.framework.reports.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Anton Sharapov
 */
@Entity
@Table(name = "subject")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {

    public enum Type {
        @JsonProperty("Organization")
        ORGAINZATION("Организации"),
        @JsonProperty("User")
        USER("Частные лица");

        public final String title;

        Type(String title) {
            this.title = title;
        }
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    public final long id;
    @Column(name = "login", unique = true, nullable = false)
    public final String login;
    @Column(name = "url", unique = true, nullable = false)
    public final String url;
    @Column(name = "avatar_url", nullable = false)
    public final String avatarUrl;
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    public final Type type;

    @JsonCreator
    public Subject(@JsonProperty(value = "id", required = true) long id,
                   @JsonProperty(value = "login", required = true) String login,
                   @JsonProperty(value = "html_url", required = true) String url,
                   @JsonProperty(value = "avatar_url", required = true) String avatarUrl,
                   @JsonProperty(value = "type", required = true) Type type) {
        this.id = id;
        this.login = login;
        this.url = url;
        this.avatarUrl = avatarUrl;
        this.type = type;
    }

    @Override
    public String toString() {
        return "[" + type.name() + " " + login + "]";
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Subject))
            return false;
        final Subject other = (Subject) obj;
        return id == other.id && Objects.equals(login, other.login) &&
                Objects.equals(url, other.url) && Objects.equals(avatarUrl, other.avatarUrl) &&
                Objects.equals(type, other.type);
    }
}
