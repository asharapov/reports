package org.echosoft.common.providers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.echosoft.common.utils.StreamUtil;

/**
 * Simple instance of the {@link BeanLoader}.
 * @author Anton Sharapov
 */
public class JdbcBeanLoader<T> implements BeanLoader<Map<String,Object>> {

    private final AttrMetaData[] meta;

    public JdbcBeanLoader(final ResultSetMetaData rsm) throws SQLException {
        this.meta = new AttrMetaData[rsm.getColumnCount()];
        for (int i = 1; i <= meta.length; i++) {
            Class fieldClass;
            try {
                fieldClass = Class.forName(rsm.getColumnClassName(i));
            } catch (ClassNotFoundException e) {
                fieldClass = null;
            }
            int precision;
            try {
                precision = rsm.getPrecision(i);
            } catch (Exception e) {
                precision = 0;      // workaround of bug in the some JDBC drivers with BLOB fields.
            }
            meta[i - 1] = new AttrMetaData(rsm.getColumnName(i).toUpperCase(), fieldClass, precision, rsm.getScale(i));
        }
    }

    /**
     * Returns name of the sql query field for specified property name of the bean.
     * This method may be used for making sql queries.
     * @param attrName name of the bean's attribute
     * @return name of the sql query field
     */
    public String getMappedField(final String attrName) {
        return attrName;
    }


    /**
     * Returns fields metadata for the specified bean class and sql result set.
     * @return fields metadata for the specified bean class and sql result set.
     */
    public AttrMetaData[] getMetadata() {
        return meta;
    }


    /**
     * Makes and populates new instance of bean from result set.
     * @param rs  jdbc ResultSet.
     * @return an instance of {@link Map} that contains all fields from result set.
     * @throws SQLException  in case of any errors occurs.
     * @throws IOException  in case of IO errors occurs during BLOBs processing.
     */
    public Map<String,Object> load(final ResultSet rs) throws SQLException, IOException {
        final ResultSetMetaData meta = rs.getMetaData();
        final int cols = meta.getColumnCount();
        final Map<String,Object> result = new HashMap<String,Object>(cols);
        for (int i=1; i<=cols; i++) {
            final String name = meta.getColumnName(i);
            final int type = meta.getColumnType(i);
            if (Types.BLOB==type) {
                byte data[] = null;
                final InputStream in = rs.getBinaryStream(i);
                if (in!=null) {
                    try {
                        data = StreamUtil.streamToBytes(in);
                    } finally {
                        in.close();
                    }
                }
                result.put(name.toUpperCase(), data);
            } else
            if (Types.DATE==type) {
                result.put(name.toUpperCase(), rs.getTimestamp(i));
            } else
                result.put(name.toUpperCase(), rs.getObject(i));
        }
        return result;
    }
}
