package org.echosoft.framework.reports.data.beans;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public class Activity implements Serializable {

    public String id;
    public String parentId;
    public String name;
    public int level;

    public Activity(String id, String parentId, String name, int level) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.level = level;
    }

    public String toString() {
        return "[Activity{id:"+id+", parent:"+parentId+", name:"+name+", level:"+level+"}]";
    }

}
