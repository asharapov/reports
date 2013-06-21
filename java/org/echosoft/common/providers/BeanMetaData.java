package org.echosoft.common.providers;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.echosoft.common.utils.BeanUtil;

/**
 * Describes fields in the bean.
 *
 * @author Anton Sharapov
 */
public class BeanMetaData implements Serializable {

    public static BeanMetaData resolveMetaData(final Object bean) {
        final AttrMetaData[] attrs;
        if (bean == null) {
            attrs = AttrMetaData.EMPTY_ARRAY;
        } else
            if (bean instanceof Map) {
                final Map map = (Map) bean;
                attrs = new AttrMetaData[map.size()];
                int i = 0;
                for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
                    final Map.Entry entry = (Map.Entry)it;
                    if (!(entry.getKey() instanceof String))
                        continue;
                    attrs[i++] = new AttrMetaData((String) entry.getKey(), entry.getValue() != null ? entry.getValue().getClass() : String.class);
                }
            } else {
                final PropertyDescriptor desc[] = BeanUtil.getPropertyDescriptors(bean.getClass());
                attrs = new AttrMetaData[desc.length];
                for (int i = 0; i < desc.length; i++) {
                    final Class cls = desc[i].getPropertyType();
                    attrs[i] = new AttrMetaData(desc[i].getName(), cls != null ? cls : Object.class);
                }
            }
        return new BeanMetaData(attrs);
    }


    private final AttrMetaData[] attrs;

    public BeanMetaData(final AttrMetaData[] attrs) {
        if (attrs == null)
            throw new IllegalArgumentException("Fields metadata must be specified");
        this.attrs = attrs;
    }

    public AttrMetaData[] getAttrs() {
        return attrs;
    }

    public AttrMetaData getAttribute(final String name) {
        for (final AttrMetaData attr : attrs) {
            if (attr.getAttrName().equals(name))
                return attr;
        }
        return null;
    }

    public boolean containsAttribute(final String name) {
        for (final AttrMetaData attr : attrs) {
            if (attr.getAttrName().equals(name))
                return true;
        }
        return false;
    }


    @Override
    public int hashCode() {
        return attrs.length;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        return Arrays.equals(attrs, ((BeanMetaData) obj).attrs);
    }
}
