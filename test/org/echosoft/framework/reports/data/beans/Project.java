package org.echosoft.framework.reports.data.beans;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Anton Sharapov
 */
public class Project implements Serializable {
    public String id;
    public String name;
    public boolean active;
    public Date started;

    public Project(String id, String name, boolean active, Date started) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.started = started!=null ? started : new Date();
    }

    public String toString() {
        return "[Project{id:"+id+", name:"+name+", active:"+active+"}]";
    }
}
