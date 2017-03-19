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

    public Invoice(String id, String name, String contragent, String invoice, int amount, double unitcost) {
        this.id = id;
        this.name = name;
        this.contragent = contragent;
        this.invoice = invoice;
        this.amount = amount;
        this.unitcost = unitcost;
    }

    public String toString() {
        return "[Invoice{id:"+ id +", name:"+name+", contragent:"+contragent+", invoice:"+invoice+", amount:"+amount+", unitcost:"+unitcost+"}]";
    }
}
