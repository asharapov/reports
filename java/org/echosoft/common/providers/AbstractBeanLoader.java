package org.echosoft.common.providers;

import java.sql.ResultSet;

/**
 * @author Anton Sharapov
 */
public abstract class AbstractBeanLoader<T> implements BeanLoader<T> {

    private final AttrMetaData metadata[];

    public AbstractBeanLoader() {
        metadata = init();
    }

    /**
     * Returns name of the sql query field for specified property name of the bean.
     * This method may be used for making sql queries.
     *
     * @param attrName name of the bean's attribute
     * @return name of the sql query field
     */
    public String getMappedField(String attrName) {
        for (AttrMetaData fm : metadata) {
            if (fm.getAttrName().equals(attrName)) {
                return fm.getMappedFieldName();
            }
        }
        return null;
    }

    /**
     * Returns fields metadata for the specified bean class and sql result set.
     *
     * @return fields metadata for the specified bean class and sql result set.
     */
    public AttrMetaData[] getMetadata() {
        return metadata;
    }

    /**
     * Makes and populates new instance of bean from result set.
     *
     * @param rs jdbc ResultSet.
     * @return object instance with populated from result set properties.
     * @throws Exception if any errors occurs
     */
    public abstract T load(ResultSet rs) throws Exception;

    protected abstract AttrMetaData[] init();

}
