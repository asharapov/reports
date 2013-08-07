package org.echosoft.framework.reports.model.el;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.echosoft.common.utils.BeanUtil;

/**
 * Содержит информацию используемую при вычислении выражений в ячейках отчета.
 * На каждый формируемый подсистемой отчет создается один и только один экземпляр данного класса.
 * @author Anton Sharapov
 */
public class ELContext {

    public static enum Scope {ROW, VAR, ENV, CONST}

    /**
     * Некоторый объект, описывающий одну строку данных (ту, которая в настоящий момент формируется в
     * выходном документе Excel).
     * Приходит от соответствующего источника данных {@link org.echosoft.framework.reports.model.providers.DataProvider}.
     */
    protected Object bean;

    /**
     * Переменные, которые вычисляются в процессе формирования отчета и могут использоваться при
     * вычислении выражений в ячейках.
     * Пример такой переменной - номер текущей (обрабатываемой) строки в формируемом документе Excel. 
     */
    protected final Map<String,Object> var;

    /**
     * Переменные окружения, для которых выполняется построение отчета.
     * Задаются программистом перед построением отчета.
     */
    protected final Map<String,Object> env;

    /**
     * Текущая локаль.
     */
    protected final Locale locale;

    protected final Map<Scope, Map<String,Object>> scopes;

    /**
     * Инициализируем контекст перед началом формирования очередного отчета значениями по умолчанию.
     */
    public ELContext() {
        this(null, null);
    }

    /**
     * Инициализируем контекст перед началом формирования очередного отчета.
     * @param locale  текущая локаль.
     * @param env  паременные окружения, которые могут быть использованы при построении данного отчета.
     */
    public ELContext(final Locale locale, final Map<String,Object> env) {
        this.locale = locale!=null ? locale : Locale.getDefault();
        this.env = env!=null ? env : new HashMap<String,Object>();
        var = new HashMap<String,Object>();
        scopes = new HashMap<Scope, Map<String,Object>>(4);
        scopes.put(Scope.ENV, this.env);
        scopes.put(Scope.VAR, this.var);
        scopes.put(Scope.ROW, new AbstractMap<String,Object>() {
                                    public Object get(final Object key) {
                                        if (key == null)
                                            throw new NullPointerException();
                                        try {
                                            return BeanUtil.getProperty(bean, (String)key);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e.getMessage(), e);
                                        }
                                    }
                                    public Set<Map.Entry<String,Object>> entrySet() {
                                        return Collections.emptySet();
//                                        throw new UnsupportedOperationException("operation not supported");
                                    }
                                    public String toString() {
                                        return "[row scope: "+bean+"]";
                                    }
                                });
        scopes.put(Scope.CONST, new AbstractMap<String,Object>() {
                                    public Object get(final Object key) {
                                        return key;
                                    }
                                    public Set<Map.Entry<String,Object>> entrySet() {
                                        throw new UnsupportedOperationException("operation not supported");
                                    }
                                    public String toString() {
                                        return "[const scope]";
                                    }
                                });
    }

    /**
     * @return  Выбранная локаль.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Возвращает модель данных для текущей строки отчета.
     * @return  объект произвольного типа, содержащий информацию полученную от поставщика данных.
     */
    public Object getRowModel() {
        return bean;
    }

    /**
     * Указывает модель данных, которая должна быть представлена в очередной строке отчета.
     * @param bean  объект произвольного типа, содержащий информацию полученную от поставщика данных.
     */
    public void setRowModel(final Object bean) {
        this.bean = bean;
    }

    public Map<String,Object> getVariables() {
        return var;
    }

    public Map<String,Object> getEnvironment() {
        return env;
    }


    /**
     * Возвращает значение атрибута из указанного пространства имен.
     * Данный метод активно используется при вычислении выражений.
     * @param attrName  имя атрибута
     * @param scope  пространство имен
     * @return  значение выбранного атрибута из указанного пространства имен.
     */
    public Object getAttribute(final String attrName, final Scope scope) {
        if (scope!=null) {
            final Map<String,Object> data = scopes.get(scope);
            return data.get(attrName);
        } else {
            try {
                return BeanUtil.getProperty(bean, attrName);
            } catch (Exception e) {
                if (var.containsKey(attrName)) {
                    return var.get(attrName);
                } else {
                    return env.get(attrName);
                }
            }
        }
    }

    /**
     * Возвращает значение атрибута из первого пространства имен в списке если это значение не null.
     * Данный метод активно используется при вычислении выражений.
     * @param attrName  имя атрибута
     * @param scopes  пространство имен
     * @return  значение выбранного атрибута из одного из указанных пространств имен или <code>null</code>
     * если указанный атрибут не присутствует ни в одном из перечисленных пространств имен.
     */
    public Object getAttribute(final String attrName, final Scope[] scopes) {
        for (int i=0; i<scopes.length; i++) {
            final Object result = this.scopes.get(scopes[i]).get(attrName);
            if (result!=null)
                return result;
        }
        return null;
    }

    /**
     * Выполняет глубокое клонирование контекста выполнения.
     * @return  копия контекста.
     */
    public ELContext cloneContext() {
        final ELContext result = new ELContext(locale, null);
        for (Map.Entry<String,Object> entry : env.entrySet()) {
            result.env.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String,Object> entry : var.entrySet()) {
            result.var.put(entry.getKey(), entry.getValue());
        }
        result.setRowModel(bean);
        return result;
    }

}
