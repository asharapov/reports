package org.echosoft.framework.reports.common.data;

import java.sql.ResultSet;

/**
 * @author Anton Sharapov
 */
public interface Loader<T> {
    T load(ResultSet rs) throws Exception;
}
