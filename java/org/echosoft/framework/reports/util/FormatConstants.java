package org.echosoft.framework.reports.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dmitry Smirnov
 */
final class FormatConstants {

    /**
     * cell type is date
     */
    public static final int DATE = 1;
    /**
     * cell type is number
     */
    public static final int NUMBER = 2;

    /* public static final String patternmap[][] = {
            {"0", ""},
            {"0.00", ""},
            {"#,##0", ""},
            {"#,##0.00", ""},
            {"($#,##0);($#,##0)", "(#,##0_);($#,##0)"},
            {"($#,##0);[Red]($#,##0)", ""},
            {"($#,##0.00);($#,##0.00)", ""},
            {"($#,##0.00);[Red]($#,##0.00)", ""},
            {"0%", ""},
            {"0.00%", ""},
            {"0.00E+00", ""},
            {"# ?/?", ""},
            {"# ??/??", ""},
            {"(#,##0_);($#,##0)", ""},
            {"(#,##0_);[Red]($#,##0)", ""},
            {"(#,##0.00_);($#,##0.00)", ""},
            {"(#,##0.00_);[Red]($#,##0.00)", ""},
            {"_(*#,##0_);_(*($#,##0);_(*\"-\"_);_(@_)", ""},
            {"_($*#,##0_);_($*($#,##0);_($*\"-\"_);_(@_)", ""},
            {"_(*#,##0_);_(*($#,##0);_(*\"-\"??_);_(@_)", ""},
            {"_($*#,##0_);_($*($#,##0);_($*\"-\"??_);_(@_)", ""},
            {"mm-mmm-yy", "MM-dd-YY"},
            {"m/d/yy", "M/d/yy"},
            {"d-mmm-yy", "d-MMM-yy"},
            {"d-mmm", "d-MMM"},
            {"mmm-yy", "MMM-yy"},
            {"h:mm AM/PM", "hh:mm a"},
            {"h:mm:ss AM/PM", "hh:mm:ss a"},
            {"h:mm", "hh:mm"},
            {"h:mm:ss", "hh:mm:ss"},
            {"m/d/yy h:mm", "M/d/yy hh:mm"}
    };

    public static final String DATE_FORMATS[] = {
        "m/d/yyyy", "[$-F800]dddd,mmmm dd,yyyy", "m/d;@", "m/d/yy;@", "mm/dd/yy;@", "[$-409]d-mmm;@", "[$-409]d-mmm-yy;@", "[$-409]dd-mmm-yy;@", "[$-409]mmm-yy;@", "[$-409]mmmm-yy;@",
        "[$-409]mmmm d, yyyy;@", "[$-409]m/d/yy h:mm AM/PM;@", "m/d/yy h:mm;@", "[$-409]mmmmm;@", "[$-409]mmmmm-yy;@", "m/d/yyyy;@", "[$-409]d-mmm-yyyy;@", "m/d/yyyy", "d-mmm-yy", "d-mmm",
        "mmm-yy", "h:mm AM/PM", "hh:mm AM/PM", "h:mm", "h:mm:ss", "m/d/yyyy h:mm", "mm:ss", "mm:ss.0", "[h]:mm:ss", "[$-409]dddd, mmmm dd, yyyy",
        "[$-F800]dddd, mmmm dd, yyyy", "m/d;@", "[-409]m/d/yy h:mm AM/PM;@", "0", "[$-409]mmmm d, yyyy;@", "[$-409]h:mm:ss AM/PM", "m/d/yy h:mm;@", "m/d/yy h:mm;@", "[$-409]d-mmm;@"
    };
    public static final short DATE_FORMAT_CODES[] = {
        14, 165, 166, 173, 174, 172, 175, 176, 177, 178,
        169, 167, 171, 179, 180, 181, 182, 14, 15, 16,
        17, 18, 19, 20, 21, 22, 45, 47, 46, 164,
        165, 166, 167, 168, 169, 170, 171, 171, 172
    };
    public static final String CURRENCY_FORMATS[] = {
        "0", "0.00", "#,##0", "#,##0.00", "($#,##0);($#,##0)", "($#,##0);[Red]($#,##0)", "($#,##0.00);($#,##0.00)", "($#,##0.00);[Red]($#,##0.00)", "0%", "0.00%",
        "0.00E+00", "# ?/?", "# ??/??", "(#,##0_);($#,##0)", "(#,##0_);[Red]($#,##0)", "(#,##0.00_);($#,##0.00)", "(#,##0.00_);[Red]($#,##0.00)", "_(*#,##0_);_(*($#,##0);_(*\"-\"_);_(@_)", "_($*#,##0_);_($*($#,##0);_($*\"-\"_);_(@_)", "_(*#,##0_);_(*($#,##0);_(*\"-\"??_);_(@_)",
        "_($*#,##0_);_($*($#,##0);_($*\"-\"??_);_(@_)"
    };


    */

    private static final String patternmap[][] = {
            //date
            {"m/d/yy", "dd.MM.yyyy"},
            {"d-mmm", "d.MMM"},
            //number
            {"#,##0", "#,##0"},
            {"0.00", "0.00"},
            {"#,##0.00", "#,##0.00"}

    };
    private static Map<String,String> formatMap = new HashMap<String,String>(patternmap.length);

    private static Map getFormatMap() {
        if (formatMap.isEmpty()) {
            for (String[] pattern : patternmap) {
                formatMap.put(pattern[0], pattern[1]);
            }
        }
        return formatMap;
    }

    /**
     *
     * @param type cell type
     * @param pattern POI data format
     * @return pattern of JAVA format
     */
    public static String getJavaFormatString(int type, String pattern) {
        String javaPattern = (String) getFormatMap().get(pattern);
        if (javaPattern != null) {
            return javaPattern;
        }

        if (type == DATE) {
            return "M/d/yy h:mm";
        } else if (type == NUMBER) {
            return "#,##0";
        } else {
            return null;
        }
    }
}

