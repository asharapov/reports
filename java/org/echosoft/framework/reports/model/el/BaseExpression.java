package org.echosoft.framework.reports.model.el;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.echosoft.common.io.FastStringWriter;
import org.echosoft.common.utils.BeanUtil;

/**
 * Описывает вычисляемые выражения, на основе которых формируется содержимое одной ячейки отчета.
 *
 * @author Anton Sharapov
 */
public class BaseExpression implements Expression {

    private static final String START_MARK = "${";
    private static final int START_MARK_LEN = START_MARK.length();
    private static final char END_MARK = '}';

    protected final Object expression;
    protected final List<Chunk> chunks;
    protected final boolean staticExpr;

    public static BaseExpression makeExpression(final Object expression) {
        return expression!=null ? new BaseExpression(expression) : null;
    }

    public BaseExpression(final Object expression) {
        this.expression = expression;
        this.chunks = new ArrayList<Chunk>(1);
        init(expression);
        this.staticExpr = (chunks.size()==1) && (chunks.get(0) instanceof StaticChunk);
    }

    public Object getRawValue() {
        return expression;
    }

    /**
     * @return true  если выражение является полностью статичным, т.е. не содержит ссылок на
     * параметры в том или ином пространстве имен контекста.
     */
    public boolean isStatic() {
        return staticExpr;
    }

    /**
     * Вычисляет выражение на основе приведенного контекста.
     *
     * @param context текущий контекст выполнения.
     * @return содержимое ячейки отчета.
     * @throws Exception  в случае возникновения каких-либо проблем.
     */
    public Object getValue(final ELContext context) throws Exception {
        final int size = chunks.size();
        if (size == 1)
            return chunks.get(0).evaluate(context);

        final FastStringWriter out = new FastStringWriter();
        for (int i = 0; i < size; i++) {
            final Chunk chunk = chunks.get(i);
            final Object value = chunk.evaluate(context);
            if (value!=null)
                out.write( value.toString() );
        }
        return out.toString();
    }


    protected void init(final Object expression) {
        final String text;
        if (expression instanceof String) {
            text = (String)expression;
        } else
        if (expression instanceof HSSFRichTextString) {
            text = ((HSSFRichTextString)expression).getString();
        } else {
            chunks.add( new StaticChunk(expression) );
            return;
        }

        if (text.indexOf(START_MARK) < 0) {
            chunks.add( new StaticChunk(expression) );
            return;
        }

        int a = 0, i, j;
        while (true) {
            i = text.indexOf(START_MARK, a);
            j = (i >= 0) ? text.indexOf(END_MARK, i) : -2;
            if (j > i) {
                if (i > a)
                    chunks.add( new StaticChunk(text.substring(a,i)) );

                String expr = text.substring(i + START_MARK_LEN, j).trim();
                MessageFormat formatter = null;
                if (expr.indexOf('(')==0) {
                    final int endPos = expr.indexOf(')',0);
                    String pattern = expr.substring(1, endPos).trim();
                    if (pattern.length()>0)
                        formatter = new MessageFormat("{0," + pattern + "}");
                    expr = expr.substring(endPos+1).trim();
                }
                chunks.add(new PatternChunk(formatter, expr));

                a = j + 1;
            } else {
                if (a < text.length())
                    chunks.add(new StaticChunk(text.substring(a)));
                break;
            }
        }
    }


    public int hashCode() {
        return expression!=null ? expression.hashCode() : 0;
    }

    public boolean equals(final Object obj) {
        if (obj==null || !obj.getClass().equals(BaseExpression.class))
            return false;
        final BaseExpression other = (BaseExpression)obj;
        return expression!=null ? expression.equals(other.expression) : other.expression==null;
    }

    public String toString() {
        return expression!=null ? expression.toString() : "<null>";
    }



    private static interface Chunk {
        public Object evaluate(ELContext context) throws Exception;
    }

    private static final class StaticChunk implements Chunk {
        private final Object value;

        public StaticChunk(Object value) {
            this.value = value;
        }

        public Object evaluate(final ELContext context) {
            return value;
        }
    }

    private static final class PatternChunk implements Chunk {
        private final MessageFormat formatter;
        private final ArrayList<ParsedExpression> expressions;

        public PatternChunk(MessageFormat formatter, String expression) {
            this.formatter = formatter;
            this.expressions = new ArrayList<ParsedExpression>(2);

            int s = 0, e = expression.length();
            for (int d=expression.indexOf('|',s);  d>=s && d<e;  d=expression.indexOf('|',s)) {
                if (d>s) {
                    expressions.add( parseExpresion(expression.substring(s,d)) );
                }
                s = d+1;
            }
            if (s<e) {
                expressions.add( parseExpresion(expression.substring(s,e)) );
            }
        }

        private ParsedExpression parseExpresion(final String expr) {
            final int ss = expr.indexOf(':',0);
            final ELContext.Scope scope;
            if (ss>=0) {
                scope = ELContext.Scope.valueOf( expr.substring(0, ss).trim().toUpperCase() );
            } else {
                scope = null;
            }
            String name = expr.substring(ss+1).trim();
            String property = null;
            final int length = name.length();
            for (int i=0; i<length; i++) {
                final char c = name.charAt(i);
                if (c==BeanUtil.NESTED_DELIM) {
                    property = name.substring(i+1);
                    name = name.substring(0, i);
                    break;
                }
            }
            return new ParsedExpression(scope, name, property);
        }

        public Object evaluate(final ELContext context) throws Exception {
            Object result = null;
            for (ParsedExpression expr : expressions) {
                result = context.getAttribute(expr.attrName, expr.scope);
                if (expr.property!=null) {
                    result = BeanUtil.getProperty(result, expr.property);
                }
                if (result!=null)
                    break;
            }

            if (formatter!=null && result!=null) {
                formatter.setLocale(context.getLocale());
                if (!(result instanceof Object[]))
                    result = new Object[]{result};
                return formatter.format(result);
            } else
                return result;
        }
    }

    private static final class ParsedExpression {
        public final ELContext.Scope scope;
        public final String attrName;
        public final String property;
        public ParsedExpression(ELContext.Scope scope, String attrName, String property) {
            if (attrName==null || attrName.length()==0)
                throw new IllegalArgumentException("attr name must be specified");
            this.scope = scope;
            this.attrName = attrName;
            this.property = property;
        }
        public String toString() {
            return "{scope:"+scope+", attr:"+attrName+", property:"+property+"}";
        }
    }

}

