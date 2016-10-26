package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Общие настройки отображения страниц документа.
 *
 * @author Anton Sharapov
 */
public class PageSettingsModel implements Serializable, Cloneable {

    /**
     * Верхний колонтитул данной страницы.
     */
    private HeaderModel header;

    /**
     * Нижний колонтитул данной страницы.
     */
    private HeaderModel footer;

    /**
     * Настройка печати.
     */
    private PrintSetupModel printSetup;

    /**
     * Отступы от края листа при печати.
     */
    private MarginsModel margins;

    /**
     * Определяет будет ли автоматически вычисляться масштаб страницы таким образом чтобы она занимала страницу целиком.
     */
    private boolean fitToPage;

    /**
     * Определяет будет ли содержимое страницы при печати отцентрованно по горизонтали.
     */
    private boolean horizontallyCenter;

    /**
     * Определяет будет ли содержимое страницы при печати отцентрованно по вертикали.
     */
    private boolean verticallyCenter;

    /**
     * Определяет масштаб отображения листа в процентах. Диапазон допустимых значений: 1..400.
     * Значение <code>null</code> (используется по умолчанию) показывает что принудительная установка масштаба выключена.
     */
    private Integer zoom;


    public PageSettingsModel() {
        this.header = new HeaderModel();
        this.footer = new HeaderModel();
        this.printSetup = new PrintSetupModel();
        this.margins = new MarginsModel();
        this.zoom = null;
    }

    /**
     * Возвращает верхний колонтитул листа отчета.
     *
     * @return заголовок листа отчета. Никогда не возвращает <code>null</code>.
     */
    public HeaderModel getHeader() {
        return header;
    }

    /**
     * Возвращает нижний колонтитул листа отчета.
     *
     * @return подвал листа отчета. Никогда не возвращает <code>null</code>.
     */
    public HeaderModel getFooter() {
        return footer;
    }

    /**
     * Возвращает настройки печати
     *
     * @return настройки печати листа. Никогда не возвращает <code>null</code>.
     */
    public PrintSetupModel getPrintSetup() {
        return printSetup;
    }

    /**
     * Возвращает величины отступов по краям листа при печати.
     *
     * @return отступы по краям листа при печати. Измеряются в дюймах.
     */
    public MarginsModel getMargins() {
        return margins;
    }


    /**
     * Возвращает масштаб отображения листа или <code>null</code> если принудительное масштабирование должно быть выключено.
     *
     * @return Масштаб отображения листа в процентах. Число в диапазоне 1..400 или <code>null</code>.
     *         Значение данного свойства по умолчанию: <code>null</code>.
     */
    public Integer getZoom() {
        return zoom;
    }
    public void setZoom(final Integer zoomInPercents) {
        if (zoomInPercents != null && (zoomInPercents < 10 || zoomInPercents > 400))
            throw new IllegalArgumentException("Illegal zoom value: " + zoomInPercents);
        this.zoom = zoomInPercents;
    }

    /**
     * Определяет будет ли автоматически вычисляться масштаб страницы таким образом чтобы она занимала страницу целиком.
     *
     * @return <code>true</code> если автоматическое масштабирование должно быть включено>
     */
    public boolean isFitToPage() {
        return fitToPage;
    }
    public void setFitToPage(final boolean fitToPage) {
        this.fitToPage = fitToPage;
    }

    /**
     * Определяет будет ли содержимое страницы при печати отцентрованно по горизонтали.
     *
     * @return <code>true</code> если центрирование по горизонтали должно быть включено.
     */
    public boolean isHorizontallyCenter() {
        return horizontallyCenter;
    }
    public void setHorizontallyCenter(final boolean horizontallyCenter) {
        this.horizontallyCenter = horizontallyCenter;
    }

    /**
     * Определяет будет ли содержимое страницы при печати отцентровано по вертикали.
     *
     * @return <code>true</code> если центрирование по вертикали должно быть включено.
     */
    public boolean isVerticallyCenter() {
        return verticallyCenter;
    }
    public void setVerticallyCenter(final boolean verticallyCenter) {
        this.verticallyCenter = verticallyCenter;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final PageSettingsModel result = (PageSettingsModel) super.clone();
        result.header = (HeaderModel) header.clone();
        result.footer = (HeaderModel) footer.clone();
        result.printSetup = (PrintSetupModel) printSetup.clone();
        result.margins = (MarginsModel) margins.clone();
        return result;
    }
}
