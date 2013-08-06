package org.echosoft.common.providers;

import org.echosoft.common.collections.issuers.ReadAheadIssuer;

/**
 * Данный интерфейс должны реализовывать все поставщики данных для построителя отчетов.
 *
 * @author Anton Sharapov
 */
public interface DataProvider<T, Q> {

    /**
     * Выполняет поиск запрашиваемых данных. Результат поиска оформляется в виде итератора который по окончании использования должен быть обязательно закрыт.
     *
     * @param query параметр использующийся для уточнения критериев отбора или сортировки запрашиваемых данных.
     *              В некоторых реализациях может быть <code>null</code> или не использоваться.
     * @return "ленивый" итератор по всем данным которые удовлетворяют заданным критериям.
     */
    public ReadAheadIssuer<T> execute(Q query) throws Exception;
}
