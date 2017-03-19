package org.echosoft.framework.reports.data.beans;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public class Company implements Serializable {

    public String id;
    public String name;
    public String director;

    public Company(String id, String name, String director) {
        this.id = id;
        this.name = name;
        this.director = director;
    }

    public String toString() {
        return "[Company{id:"+id+", name:"+name+", director:"+director+"}]";
    }
}
