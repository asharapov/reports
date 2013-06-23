package org.echosoft.common.providers;

import java.sql.ResultSet;

/**
 * Реализации данного интерфейса отвечают за наиболее подходящий способ представления
 * записей полученных из базы данных.
 *
 * @author Anton Sharapov
 */
public interface BeanLoader<T> {

    /**
     * Создает экземпляр объекта определенного класса и инициализирует его данными полученными в текущей строке курсора.
     *
     * @param rs курсор набора данных полученных из базы.
     * @return экземпляр объекта инициализированного на основе полученного курсора.
     * @throws Exception в случае каких-либо проблем.
     */
    public T load(ResultSet rs) throws Exception;
}
