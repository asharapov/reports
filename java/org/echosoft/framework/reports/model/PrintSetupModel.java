package org.echosoft.framework.reports.model;

import java.io.Serializable;

/**
 * Данный класс содержит настройки отчета для печати.
 *
 * @author Dmitry Smirnov
 */
public class PrintSetupModel implements Serializable, Cloneable {

    private short paperSize;
    private short scale;
    private short fitWidth;
    private short pageStart;
    private short fitHeight;
    private double footerMargin;
    private boolean landscape;
    private boolean leftToRight;
    private boolean noColor;
    private boolean draft;
    private short hResolution;
    private boolean notes;
    private boolean usePage;
    private short vResolution;
    private short copies;
    private double headerMargin;
    private boolean validSettings;
    private boolean noOrientation;


    public void setPaperSize(short paperSize) {
        this.paperSize = paperSize;
    }

    public void setScale(short scale) {
        this.scale = scale;
    }

    public void setFitWidth(short fitWidth) {
        this.fitWidth = fitWidth;
    }

    public void setPageStart(short pageStart) {
        this.pageStart = pageStart;
    }

    public void setFitHeight(short fitHeight) {
        this.fitHeight = fitHeight;
    }

    public void setFooterMargin(double footerMargin) {
        this.footerMargin = footerMargin;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public void setLeftToRight(boolean leftToRight) {
        this.leftToRight = leftToRight;
    }

    public void setNoColor(boolean noColor) {
        this.noColor = noColor;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public void setHResolution(short hResolution) {
        this.hResolution = hResolution;
    }

    public void setNotes(boolean notes) {
        this.notes = notes;
    }

    public void setUsePage(boolean usePage) {
        this.usePage = usePage;
    }

    public void setVResolution(short vResolution) {
        this.vResolution = vResolution;
    }

    public void setCopies(short copies) {
        this.copies = copies;
    }

    public void setHeaderMargin(double headerMargin) {
        this.headerMargin = headerMargin;
    }

    public void setValidSettings(boolean validSettings) {
        this.validSettings = validSettings;
    }

    public void setNoOrientation(boolean noOrientation) {
        this.noOrientation = noOrientation;
    }

    /**
     * Returns the paper size.
     *
     * @return paper size
     */
    public short getPaperSize() {
        return paperSize;
    }

    /**
     * Returns the scale.
     *
     * @return scale
     */
    public short getScale() {
        return scale;
    }

    /**
     * Returns the page start.
     *
     * @return page start
     */
    public short getPageStart() {
        return pageStart;
    }

    /**
     * Returns the number of pages wide to fit sheet in.
     *
     * @return number of pages wide to fit sheet in
     */
    public short getFitWidth() {
        return fitWidth;
    }

    /**
     * Returns the number of pages high to fit the sheet in.
     *
     * @return number of pages high to fit the sheet in
     */
    public short getFitHeight() {
        return fitHeight;
    }

    /**
     * Returns the left to right print order.
     *
     * @return left to right print order
     */
    public boolean getLeftToRight() {
        return leftToRight;
    }

    /**
     * Returns the landscape mode.
     *
     * @return landscape mode
     */
    public boolean getLandscape() {
        return landscape;
    }

    /**
     * Returns the valid settings.
     *
     * @return valid settings
     */
    public boolean getValidSettings() {
        return validSettings;
    }

    /**
     * Returns the black and white setting.
     *
     * @return black and white setting
     */
    public boolean getNoColor() {
        return noColor;
    }

    /**
     * Returns the draft mode.
     *
     * @return draft mode
     */
    public boolean getDraft() {
        return draft;
    }

    /**
     * Returns the print notes.
     *
     * @return print notes
     */
    public boolean getNotes() {
        return notes;
    }

    /**
     * Returns the no orientation.
     *
     * @return no orientation
     */
    public boolean getNoOrientation() {
        return noOrientation;
    }

    /**
     * Returns the use page numbers.
     *
     * @return use page numbers
     */
    public boolean getUsePage() {
        return usePage;
    }

    /**
     * Returns the horizontal resolution.
     *
     * @return horizontal resolution
     */
    public short getHResolution() {
        return hResolution;
    }

    /**
     * Returns the vertical resolution.
     *
     * @return vertical resolution
     */
    public short getVResolution() {
        return vResolution;
    }

    /**
     * Returns the header margin.
     *
     * @return header margin
     */
    public double getHeaderMargin() {
        return headerMargin;
    }

    /**
     * Returns the footer margin.
     *
     * @return footer margin
     */
    public double getFooterMargin() {
        return footerMargin;
    }

    /**
     * Returns the number of copies.
     *
     * @return number of copies
     */
    public short getCopies() {
        return copies;
    }


    @Override
    public int hashCode() {
        return paperSize + 31 * scale;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final PrintSetupModel other = (PrintSetupModel) obj;
        return copies == other.copies &&
                draft == other.draft &&
                fitHeight == other.fitHeight &&
                fitWidth == other.fitWidth &&
                landscape == other.landscape &&
                leftToRight == other.leftToRight &&
                noColor == other.noColor &&
                noOrientation == other.noOrientation &&
                notes == other.notes &&
                pageStart == other.pageStart &&
                paperSize == other.paperSize &&
                scale == other.scale &&
                usePage == other.usePage &&
                hResolution == other.hResolution &&
                vResolution == other.vResolution &&
                Double.compare(other.headerMargin, headerMargin) == 0 &&
                Double.compare(other.footerMargin, footerMargin) == 0 &&
                validSettings == other.validSettings;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
