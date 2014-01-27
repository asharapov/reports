package org.echosoft.framework.reports.macros;

import java.util.HashMap;

import org.echosoft.common.utils.StringUtil;

/**
 * Реестр всех глобальных макро-функций представляющих собой особые алгоритмы по формированию значений в указанных ячейках отчета.
 * Разработчики могут определять в этом классе все те макросы которые будут доступны для генерации любых отчетов.
 * В противоположность этому, разработчики могут зарегистрировать макросы в модели конкретного отчета. В таком случае
 * они будут доступны только при формировании экземпляров именно этого отчета.
 *
 * @author Anton Sharapov
 */
public final class MacrosRegistry {

    private static final HashMap<String, Macros> map = new HashMap<String, Macros>();
    static {
        MacrosRegistry.registerMacros("gsum", new GroupSum());
        MacrosRegistry.registerMacros("gmax", new GroupMax());
        MacrosRegistry.registerMacros("gmin", new GroupMin());
        MacrosRegistry.registerMacros("gavg", new GroupAvg());
        MacrosRegistry.registerMacros("gitemnum", new GroupItemNum());
        MacrosRegistry.registerMacros("nrowsum", new NRowsSum());
        MacrosRegistry.registerMacros("fnrowsum", new FNRowsSum());
        MacrosRegistry.registerMacros("fnrowsumprod", new FNRowsSumProd());
        MacrosRegistry.registerMacros("fcolrowssum", new FColRowsSum());
    }

    /**
     * Регистрирует новую макро-функцию под определенным именем.
     *
     * @param name  имя под которым данный макрос будет доступен в системе. Не может быть пустой строкой или null.
     * @param func  собственно макрос.
     */
    public static void registerMacros(String name, Macros func) {
        name = StringUtil.trim(name);
        if (name==null || func==null)
            throw new IllegalArgumentException("All arguments must be specified");

        map.put(name, func);
    }

    /**
     * Возвращает макро-функцию по ее имени.
     *
     * @param name  имя функции (чуствительно к регистру).
     * @return  Соответствующая функция или <code>null</code>.
     */
    public static Macros getMacros(String name) {
        return map.get(name);
    }

}
