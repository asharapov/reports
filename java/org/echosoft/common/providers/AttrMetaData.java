package org.echosoft.common.providers;

import java.io.Serializable;

/**
 * Содержит описание отдельного атрибута объекта.
 *
 * @author Anton Sharapov
 */
public class AttrMetaData implements Serializable {

    public static final AttrMetaData EMPTY_ARRAY[] = new AttrMetaData[0];

    private final String attrName;
    private final Class attrClass;
    private final int attrPrecision;
    private final int attrScale;
    private final String mappedFieldName;

    public AttrMetaData(final String attrName, final Class attrClass) {
        this(attrName, attrClass, 0, 0, attrName);
    }

    public AttrMetaData(final String attrName, final Class attrClass, final String mappedFieldName) {
        this(attrName, attrClass, 0, 0, mappedFieldName);
    }

    public AttrMetaData(final String attrName, final Class attrClass, final int attrPrecision, final int attrScale) {
        this(attrName, attrClass, attrPrecision, attrScale, attrName);
    }

    public AttrMetaData(final String attrName, final Class attrClass, final int attrPrecision, final int attrScale, final String mappedFieldName) {
        if (attrName == null || attrClass == null)
            throw new NullPointerException("Name and class for field must be specified");
        this.attrName = attrName;
        this.attrClass = attrClass;
        this.attrPrecision = attrPrecision;
        this.attrScale = attrScale;
        this.mappedFieldName = mappedFieldName;
    }

    /**
     * @return Название атрибута.
     */
    public String getAttrName() {
        return attrName;
    }

    /**
     * @return класс атрибута.
     */
    public Class getAttrClass() {
        return attrClass;
    }

    /**
     * Применяется для числовых типов.
     *
     * @return максимально допустимое кол-во цифр перед запятой.
     */
    public int getAttrPrecision() {
        return attrPrecision;
    }

    /**
     * Применяется для числовых типов.
     *
     * @return максимально допустимое кол-во цифр после запятой.
     */
    public int getAttrScale() {
        return attrScale;
    }

    /**
     * Gets the mapped name for given field in the persistence storage.
     * By default it same as field name.
     *
     * @return mapped name for given field in the persistence storage.
     * @since 0.7.6
     */
    public String getMappedFieldName() {
        return mappedFieldName;
    }


    @Override
    public int hashCode() {
        return attrName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final AttrMetaData other = (AttrMetaData) obj;
        return attrName.equals(other.attrName) && attrClass.equals(other.attrClass) &&
                attrPrecision == other.attrPrecision && attrScale == other.attrScale &&
                (mappedFieldName != null ? mappedFieldName.equals(other.mappedFieldName) : other.mappedFieldName == null);
    }

    @Override
    public String toString() {
        return "[AttrMeta{name:" + attrName + ", class:" + attrClass + ", precision:" + attrPrecision + ", scale:" + attrScale + ", mapped:" + mappedFieldName + "}]";
    }
}
