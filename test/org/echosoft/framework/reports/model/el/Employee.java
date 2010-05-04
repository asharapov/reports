package org.echosoft.framework.reports.model.el;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Anton Sharapov
 */
public class Employee implements Serializable {

    public final String name;
    public final String title;
    public final Date born;
    public final int age;

    public Employee(String name, String title, Date born) {
        this.name = name!=null ? name : "";
        this.title = title;
        this.born = born;
        if (born!=null) {
            final Calendar cal = Calendar.getInstance();
            final int now = cal.get(Calendar.YEAR);
            cal.setTime(born);
            this.age = now - cal.get(Calendar.YEAR);
        } else {
            this.age = 0;
        }
    }
}
