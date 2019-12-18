package org.echosoft.framework.reports.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Сведения о лицензиях.
 *
 * @author Anton Sharapov
 */
@Entity
@Table(name = "license")
@JsonIgnoreProperties(ignoreUnknown = true)
public class License {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    public String key;

    @Column(name = "name", unique = true, nullable = false)
    public String name;

    @Column(name = "url")
    public String url;

    protected License() {
    }

    @JsonCreator
    public License(
            @JsonProperty(value = "key", required = true) String key,
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty(value = "url", required = true) String url) {
        this.key = key;
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return "[License{name:" + name + "}]";
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof License))
            return false;
        final License other = (License) obj;
        return Objects.equals(key, other.key) && Objects.equals(name, other.name) && Objects.equals(url, other.url);
    }
}
