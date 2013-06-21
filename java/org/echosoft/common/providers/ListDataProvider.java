package org.echosoft.common.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.echosoft.common.data.Query;

/**
 * @author Anton Sharapov
 */
public class ListDataProvider<T> implements DataProvider {

    private final List<T> rows;

    public ListDataProvider() {
        rows = Collections.emptyList();
    }

    public ListDataProvider(final T[] beans) {
        this.rows = beans != null ?
                new ArrayList<T>(Arrays.asList(beans))
                : Collections.<T>emptyList();
    }

    public ListDataProvider(final List<T> beans) {
        this.rows = beans != null
                ? new ArrayList<T>(beans)
                : Collections.<T>emptyList();
    }

    public ListDataProvider(final Iterator<T> iter) {
        this.rows = new ArrayList<T>();
        if (iter != null) {
            while (iter.hasNext()) {
                rows.add(iter.next());
            }
        }
    }

    @Override
    public BeanIterator<T> execute(final Query query) {
        final List<T> list;
        if (query != null && query.hasSortCriteria()) {
            list = new ArrayList<T>(rows);
            Collections.sort(list, new BeanComparator<T>(query.getSortCriteriaAsArray()));
        } else {
            list = rows;
        }
        return new ListBeanIterator<T>(list);
    }

}
