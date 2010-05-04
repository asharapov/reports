package org.echosoft.framework.reports.data.beans;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public class Payment implements Serializable {

    public Activity activity;
    public Company company;
    public Project project;
    public Invoice invoice;

    public Payment(Activity activity, Company company, Project project, Invoice invoice) {
        this.activity = activity;
        this.company = company;
        this.project = project;
        this.invoice = invoice;
    }

    public String toString() {
        return "[Payment{activity:"+activity.id+", company:"+company.id+", project:"+project.id+", invoice:"+invoice.name+"}]";
    }
}
