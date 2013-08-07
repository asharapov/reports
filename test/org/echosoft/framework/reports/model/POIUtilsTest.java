package org.echosoft.framework.reports.model;

import java.util.Arrays;
import java.util.List;

import org.echosoft.framework.reports.util.POIUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class POIUtilsTest {


    @Test
    public void testGetColumnNumber() {
        Assert.assertEquals(0, POIUtils.getColumnNumber("A"));
        Assert.assertEquals(1, POIUtils.getColumnNumber("B"));
        Assert.assertEquals(2, POIUtils.getColumnNumber("C"));
        Assert.assertEquals(25, POIUtils.getColumnNumber("Z"));
        Assert.assertEquals(26, POIUtils.getColumnNumber("AA"));
        Assert.assertEquals(27, POIUtils.getColumnNumber("AB"));
        Assert.assertEquals(28, POIUtils.getColumnNumber("AC"));
        Assert.assertEquals(51, POIUtils.getColumnNumber("AZ"));
        Assert.assertEquals(52, POIUtils.getColumnNumber("BA"));
        Assert.assertEquals(53, POIUtils.getColumnNumber("BB"));
        Assert.assertEquals(0, POIUtils.getColumnNumber("a"));
        Assert.assertEquals(1, POIUtils.getColumnNumber("b"));
        Assert.assertEquals(2, POIUtils.getColumnNumber("c"));
        Assert.assertEquals(25, POIUtils.getColumnNumber("z"));
        Assert.assertEquals(26, POIUtils.getColumnNumber("aa"));
        Assert.assertEquals(27, POIUtils.getColumnNumber("ab"));
        Assert.assertEquals(28, POIUtils.getColumnNumber("ac"));
        Assert.assertEquals(51, POIUtils.getColumnNumber("az"));
        Assert.assertEquals(52, POIUtils.getColumnNumber("ba"));
        Assert.assertEquals(53, POIUtils.getColumnNumber("bB"));
        Assert.assertEquals(-1, POIUtils.getColumnNumber(""));
        Assert.assertEquals(-1, POIUtils.getColumnNumber(" "));
        Assert.assertEquals(-1, POIUtils.getColumnNumber("-"));
        Assert.assertEquals(-1, POIUtils.getColumnNumber("A."));
        Assert.assertEquals(-1, POIUtils.getColumnNumber("b2"));
    }

    @Test
    public void testParams() throws Exception {
        process(1, 5, Arrays.asList("A", "B", "C"), 2, 1);
    }

    public static String process(int top, int bottom, List<String> colnames, int nth, int offset) {
        final StringBuilder formula = new StringBuilder(32);
        if (top > bottom) {
            return "0";
        } else if (nth == 1) {
            formula.append("SUMPRODUCT(");
            for (int i = 0, cnt = colnames.size(); i < cnt; i++) {
                if (i > 0)
                    formula.append(',');
                final String colname = colnames.get(i);
                formula.append(colname).append(top).append(':').append(colname).append(bottom);
            }
            formula.append(')');
        } else {
            formula.append("SUMPRODUCT(");
            formula.append("ABS(MOD(ROW(").append(top).append(':').append(bottom).append(")-ROW(A").append(top).append("),").append(nth).append(")=").append(offset).append(")");
            for (int i = 0, cnt = colnames.size(); i < cnt; i++) {
                formula.append(',');
                final String colname = colnames.get(i);
                formula.append(colname).append(top).append(':').append(colname).append(bottom);
            }
            formula.append(')');
        }
        System.out.println(formula);
        return formula.toString();
    }
}
