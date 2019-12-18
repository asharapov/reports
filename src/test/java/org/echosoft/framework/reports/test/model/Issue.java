package org.echosoft.framework.reports.test.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Anton Sharapov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {

    enum State {
        @JsonProperty("open")
        OPEN,
        @JsonProperty("closed")
        CLOSED
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {
        public final long id;
        public final String name;
        public final String color;
        public final boolean isDefault;

        @JsonCreator
        public Label(@JsonProperty(value = "id", required = true) final long id,
                     @JsonProperty(value = "name", required = true) final String name,
                     @JsonProperty(value = "color", required = true) final String color,
                     @JsonProperty(value = "default", required = true) final boolean isDefault) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.isDefault = isDefault;
        }

        @Override
        public String toString() {
            return "[Label{name:" + name + ", color:" + color + ", default:" + isDefault + "}]";
        }
    }

    @JsonProperty(value = "id", required = true)
    public long id;

    @JsonProperty(value = "number", required = true)
    public int number;

    @JsonProperty(value = "title", required = true)
    public String title;

    @JsonProperty(value = "user", required = true)
    public Subject user;

    @JsonProperty(value = "body", required = true)
    public String body;

    @JsonProperty(value = "state", required = true)
    public State state;

    @JsonProperty(value = "created_at", required = true)
    public Date createdAt;

    @JsonProperty(value = "updated_at", required = true)
    public Date updatedAt;

    @JsonProperty(value = "closed_at", required = true)
    public Date closedAt;

    @JsonProperty(value = "labels")
    public List<Label> labels;

}
