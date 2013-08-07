package org.echosoft.framework.reports.model;

import java.io.Serializable;

import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;

/**
 * Дополнительное описание отчета. Транслируется в соответствующие свойства итогового документа excel.
 *
 * @author Anton Sharapov
 */
public class ReportDescription implements Serializable, Cloneable {

    private Expression company;
    private Expression application;
    private Expression author;
    private Expression version;
    private Expression title;
    private Expression subject;
    private Expression category;
    private Expression comments;


    public Expression getCompany() {
        return company;
    }
    public void setCompany(final Expression company) {
        this.company = company;
    }

    public Expression getApplication() {
        return application;
    }
    public void setApplication(final Expression application) {
        this.application = application;
    }

    public Expression getAuthor() {
        return author;
    }
    public void setAuthor(final Expression author) {
        this.author = author;
    }

    public Expression getVersion() {
        return version;
    }
    public void setVersion(final Expression version) {
        this.version = version;
    }

    public Expression getTitle() {
        return title;
    }
    public void setTitle(final Expression title) {
        this.title = title;
    }

    public Expression getSubject() {
        return subject;
    }
    public void setSubject(final Expression subject) {
        this.subject = subject;
    }

    public Expression getCategory() {
        return category;
    }
    public void setCategory(final Expression category) {
        this.category = category;
    }

    public Expression getComments() {
        return comments;
    }
    public void setComments(final Expression comments) {
        this.comments = comments;
    }


    public String getCompany(final ELContext ctx) throws Exception {
        final Object result;
        return (company != null && (result = company.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }

    public String getApplication(final ELContext ctx) throws Exception {
        final Object result;
        return (application != null && (result = application.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }

    public String getAuthor(final ELContext ctx) throws Exception {
        final Object result;
        return (author != null && (result = author.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }

    public String getVersion(final ELContext ctx) throws Exception {
        final Object result;
        return (version != null && (result = version.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }

    public String getTitle(final ELContext ctx) throws Exception {
        final Object result;
        return (title != null && (result = title.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }

    public String getSubject(final ELContext ctx) throws Exception {
        final Object result;
        return (subject != null && (result = subject.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }

    public String getCategory(final ELContext ctx) throws Exception {
        final Object result;
        return (category != null && (result = category.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }

    public String getComments(final ELContext ctx) throws Exception {
        final Object result;
        return (comments != null && (result = comments.getValue(ctx)) != null)
                ? result.toString()
                : null;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
