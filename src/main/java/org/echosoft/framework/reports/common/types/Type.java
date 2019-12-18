package org.echosoft.framework.reports.common.types;

import java.io.Serializable;

/**
 * Описывает типы данных, которые можно сериализовать в строки и наоборот.
 *
 * @author Anton Sharapov
 */
public interface Type<T> extends Serializable {

    /**
     * Целевой java класс.
     * @return класс соответствующего java бина
     */
    public Class<T> getTarget();

    /**
     * Проверяет что указанный в аргументе объект относится к соответствующему классу.
     * @param obj  объект, чей класс требуется проверить на соответствие данному типу.
     * @return  <code>true</code> если класс объекта в аргументе равен целевому классу типа или является его потомком.
     */
    public boolean instanceOf(Object obj);

    /**
     * Сериализует экземпляр соответствующего класса в строку.
     * @param value  экземпляр соответствующего класса. Может быть <code>null</code>.
     * @return  Сериализованное значение объекта или <code>null</code> если аргумент быть <code>null</code>.
     * @throws Exception  в случае неверного класса аргумента.
     */
    public String encode(T value) throws Exception;

    /**
     * Десериализует строку в экземпляр соответствующего класса.
     * @param str  строка из которой требуется восстановить объект соответствующего класса. Может быть <code>null</code>.
     * @return  Восстановленное значение объекта соответствующего класса или <code>null</code> если переданная в аргументе строка была <code>null</code>.
     * @throws Exception  в случае невозможности восстановления объекта из указанной строки.
     */
    public T decode(String str) throws Exception;

}
