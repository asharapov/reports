package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Описывает верхний или нижний колонтитулы листа отчета.
 *
 * @author Anton Sharapov
 */
public class HeaderModel implements Serializable, Cloneable {

    private String left;
    private String center;
    private String right;

    public String getLeft() {
        return left;
    }
    public void setLeft(final String left) {
        this.left = left;
    }

    public String getCenter() {
        return center;
    }
    public void setCenter(final String center) {
        this.center = center;
    }

    public String getRight() {
        return right;
    }
    public void setRight(final String right) {
        this.right = right;
    }

    @Override
    public int hashCode() {
        if (left != null) {
            return left.hashCode();
        } else
        if (center != null) {
            return center.hashCode();
        } else
        if (right != null) {
            return right.hashCode();
        } else
            return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final HeaderModel other = (HeaderModel) obj;
        return (left != null ? left.equals(other.left) : other.left == null) &&
                (center != null ? center.equals(other.center) : other.center == null) &&
                (right != null ? right.equals(other.right) : other.right == null);
    }

    @Override
    public String toString() {
        return "[Header{left:" + left + ", center:" + center + ", right:" + right + "}]";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
