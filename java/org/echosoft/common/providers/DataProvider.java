package org.echosoft.common.providers;

import org.echosoft.common.data.Query;

/**
 * Provides queried data using various rules, such as additional constraints on retrieving data,
 * sorting rules, paging support.
 * @author Anton Sharapov
 */
public interface DataProvider<T> {

    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return lazy iterator through queried dataset.
     * @throws DataProviderException  in case if any errors occurs.
     */
    public BeanIterator<T> execute(Query query) throws Exception;

}
