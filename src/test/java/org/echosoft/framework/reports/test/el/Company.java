package org.echosoft.framework.reports.test.el;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Anton Sharapov
 */
public class Company implements Serializable {

    public final String name;
    public final String address;
    private final List<Employee> employee;

    public Company(String name, String address, Employee[] employee) {
        this.name = name;
        this.address = address;
        this.employee = new ArrayList<Employee>();
        if (employee!=null) {
            this.employee.addAll( Arrays.asList(employee) );
        }
    }

    public int getEmployeeCount() {
        return employee.size();
    }

    public void addEmployee(String name, String title, Date born) {
        this.employee.add( new Employee(name, title, born) );
    }

    public Employee getEmployeeByName(String name) {
        for (Employee emp : employee) {
            if (emp.name!=null && emp.name.equals(name))
                return emp;
        }
        return null;
    }

    public List<Employee> getEmployee() {
        return employee;
    }
}
