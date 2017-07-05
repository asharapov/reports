package org.echosoft.framework.reports.data.beans;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public class Invoice implements Serializable {
    public String id;
    public String name;
    public String contragent;
    public String invoice;
    public int amount;
    public double unitcost;
    public String comment;

    public Invoice(String id, String name, String contragent, String invoice, int amount, double unitcost, String comment) {
        this.id = id;
        this.name = name;
        this.contragent = contragent;
        this.invoice = invoice;
        this.amount = amount;
        this.unitcost = unitcost;
        this.comment = comment;
    }

    public String toString() {
        return "[Invoice{id:"+ id +", name:"+name+", contragent:"+contragent+", invoice:"+invoice+", amount:"+amount+", unitcost:"+unitcost+"}]";
    }
}
