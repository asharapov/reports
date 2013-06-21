package org.echosoft.common.providers;

import java.sql.ResultSet;

/**
 * This interface is responsible for populating bean instances from sql result sets.
 * @author Anton Sharapov
 */
public interface BeanLoader<T> {

    /**
     * Returns name of the sql query field for specified property name of the bean.
     * This method may be used for making sql queries.
     * @param attrName name of the bean's attribute
     * @return name of the sql query field
     */
    public String getMappedField(String attrName);

    /**
     * Returns fields metadata for the specified bean class and sql result set.
     * @return fields metadata for the specified bean class and sql result set.
     */
    public AttrMetaData[] getMetadata();

    /**
     * Makes and populates new instance of bean from result set.
     * @param rs  jdbc ResultSet.
     * @return  object instance with populated from result set properties.
     * @throws Exception if any errors occurs
     */
    public T load(ResultSet rs) throws Exception;

}
