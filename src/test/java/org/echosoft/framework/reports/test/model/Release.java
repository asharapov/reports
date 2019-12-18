package org.echosoft.framework.reports.test.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Anton Sharapov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Release {

    @JsonProperty(value = "id", required = true)
    public long id;
    @JsonProperty(value = "html_url", required = true)
    public String url;
    @JsonProperty(value = "tag_name", required = true)
    public String tagName;
    @JsonProperty(value = "name", required = true)
    public String name;
    @JsonProperty(value = "target_commitish", required = true)
    public String target;
    @JsonProperty(value = "draft", required = true)
    public boolean draft;
    @JsonProperty(value = "author", required = true)
    public Subject author;
    @JsonProperty(value = "prerelease", required = true)
    public boolean prerelease;
    @JsonProperty(value = "created_at", required = true)
    public Date createdAt;
    @JsonProperty(value = "published_at", required = true)
    public Date publishedAt;
    @JsonProperty(value = "tarball_url", required = true)
    public String tarballUrl;
    @JsonProperty(value = "zipball_url", required = true)
    public String zipballUrl;
    @JsonProperty(value = "body", required = true)
    public String body;

    @Override
    public String toString() {
        return "[Release{name:" + name + ", created:" + createdAt + "}]";
    }
}
