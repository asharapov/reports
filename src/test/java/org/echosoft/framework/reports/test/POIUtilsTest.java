package org.echosoft.framework.reports.test;

import org.echosoft.framework.reports.util.POIUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Anton Sharapov
 */
public class POIUtilsTest {


    @Test
    void testGetColumnNumber() {
        assertEquals(0, POIUtils.getColumnNumber("A"));
        assertEquals(1, POIUtils.getColumnNumber("B"));
        assertEquals(2, POIUtils.getColumnNumber("C"));
        assertEquals(25, POIUtils.getColumnNumber("Z"));
        assertEquals(26, POIUtils.getColumnNumber("AA"));
        assertEquals(27, POIUtils.getColumnNumber("AB"));
        assertEquals(28, POIUtils.getColumnNumber("AC"));
        assertEquals(51, POIUtils.getColumnNumber("AZ"));
        assertEquals(52, POIUtils.getColumnNumber("BA"));
        assertEquals(53, POIUtils.getColumnNumber("BB"));
        assertEquals(0, POIUtils.getColumnNumber("a"));
        assertEquals(1, POIUtils.getColumnNumber("b"));
        assertEquals(2, POIUtils.getColumnNumber("c"));
        assertEquals(25, POIUtils.getColumnNumber("z"));
        assertEquals(26, POIUtils.getColumnNumber("aa"));
        assertEquals(27, POIUtils.getColumnNumber("ab"));
        assertEquals(28, POIUtils.getColumnNumber("ac"));
        assertEquals(51, POIUtils.getColumnNumber("az"));
        assertEquals(52, POIUtils.getColumnNumber("ba"));
        assertEquals(53, POIUtils.getColumnNumber("bB"));
        assertEquals(-1, POIUtils.getColumnNumber(""));
        assertEquals(-1, POIUtils.getColumnNumber(" "));
        assertEquals(-1, POIUtils.getColumnNumber("-"));
        assertEquals(-1, POIUtils.getColumnNumber("A."));
        assertEquals(-1, POIUtils.getColumnNumber("b2"));
    }

}
