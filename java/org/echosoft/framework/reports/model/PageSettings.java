package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Общие настройки отображения страниц документа.
 * @author Anton Sharapov
 */
public class PageSettings implements Serializable, Cloneable {

    /**
     * Верхний колонтитул данной страницы.
     */
    private Header header;

    /**
     * Нижний колонтитул данной страницы.
     */
    private Header footer;

    /**
     * Настройка печати.
     */
    private PrintSetup printSetup;

    /**
     * Отступы от края листа при печати.
     */
    private Margins margins;

    public PageSettings() {
        this.header = new Header();
        this.footer = new Header();
        this.printSetup = new PrintSetup();
        this.margins = new Margins();
    }

    /**
     * Возвращает верхний колонтитул листа отчета.
     *
     * @return заголовок листа отчета. Никогда не возвращает <code>null</code>.
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Возвращает нижний колонтитул листа отчета.
     *
     * @return подвал листа отчета. Никогда не возвращает <code>null</code>.
     */
    public Header getFooter() {
        return footer;
    }

    /**
     * Возвращает настройки печати
     *
     * @return настройки печати листа. Никогда не возвращает <code>null</code>.
     */
    public PrintSetup getPrintSetup() {
        return printSetup;
    }

    /**
     * Возвращает величины отступов по краям листа при печати.
     * @return  отступы по краям листа при печати. Измеряются в дюймах.
     */
    public Margins getMargins() {
        return margins;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        final PageSettings result = (PageSettings)super.clone();
        result.header = (Header)header.clone();
        result.footer = (Header)footer.clone();
        result.printSetup = (PrintSetup)printSetup.clone();
        result.margins = (Margins)margins.clone();
        return result;
    }
}
