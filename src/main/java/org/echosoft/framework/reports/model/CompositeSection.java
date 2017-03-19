package org.echosoft.framework.reports.model;

import java.util.ArrayList;
import java.util.List;

import org.echosoft.framework.reports.model.providers.ProviderUsage;
import org.echosoft.framework.reports.util.POIUtils;

/**
 * Представляет собой комбинацию секций которые логически связаны между собой и есть необходимость выполнять
 * над всеми ними какие-то общие действия.
 * (пр: показывать или не показывать в отчете, использовать обработчики событий, ...).
 *
 * @author Anton Sharapov
 */
public class CompositeSection extends Section {

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * Определяет режим работы с поставщиком данных определенным в данной секции.
     */
    private ProviderUsage providerUsage;

    /**
     * Вся информация о группировках данных в секции.
     */
    private List<GroupModel> groups;

    /**
     * Перечень порядковых номеров колонок (начиная с 0) в ячейках которых необходимо делать отступы
     * согласно с текущим уровнем группировки.
     * <p>PS: пока не используется</p>
     */
    private int[] indentedColumns;

    /**
     * Дочерние секции.
     */
    private List<Section> sections;

    public CompositeSection(final String id) {
        super(id);
        providerUsage = ProviderUsage.STANDARD;
        groups = new ArrayList<>(5);
        indentedColumns = EMPTY_INT_ARRAY;
        sections = new ArrayList<>();
    }

    /**
     * Возвращает режим использования поставщика данных в секции. Если таковой режим не был указан в модели явно то метод вернет значение данного свойства по умолчанию.
     *
     * @return выбранный режим работы с поставщиком данных в данной секции.
     */
    public ProviderUsage getProviderUsage() {
        return providerUsage;
    }
    /**
     * Устанавливает режим использования поставщика данных в секции.
     *
     * @param providerUsage режим использования поставщика данных. Если указан <code>null</code> то будет использоваться режим по умолчанию.
     */
    public void setProviderUsage(final ProviderUsage providerUsage) {
        this.providerUsage = providerUsage != null ? providerUsage : ProviderUsage.STANDARD;
    }

    /**
     * Возвращает информацию о том по каким полям и в какой последовательсности следует группировать строки в секции.
     *
     * @return список объектов {@link GroupModel}.
     */
    public List<GroupModel> getGroups() {
        return groups;
    }

    /**
     * возвращает перечень порядковых номеров колонок (начиная с 0) в ячейках которых необходимо делать отступы
     * согласно текущему уровню группировки.
     *
     * @return перечень номеров колонок в ячейках которых надо делать дополнительные отступы.
     */
    public int[] getIndentedColumns() {
        return indentedColumns;
    }

    /**
     * Устанавливает перечень колонок в ячейках которых необходимо делать отступы согласно текущему уровню группировки.
     *
     * @param indentedColumns перечень порядковых номеров колонок (начиная с 0).
     */
    public void setIndentedColumns(final int[] indentedColumns) {
        this.indentedColumns = indentedColumns != null ? indentedColumns : EMPTY_INT_ARRAY;
    }

    /**
     * Устанавливает перечень колонок в ячейках которых необходимо делать отступы согласно текущему уровню группировки.
     *
     * @param indentedColumns перечень названий колонок в формате в том формате в котором они представлены в UI (A, B, C, .. Z, AA, AB, ..)
     */
    public void setIndentedColumns(final String[] indentedColumns) {
        if (indentedColumns == null) {
            this.indentedColumns = EMPTY_INT_ARRAY;
        } else {
            this.indentedColumns = new int[indentedColumns.length];
            for (int i = 0; i < indentedColumns.length; i++) {
                this.indentedColumns[i] = POIUtils.getColumnNumber(indentedColumns[i]);
            }
        }
    }


    /**
     * Осуществляет рекурсивный поиск дочерней секции по ее идентификатору.
     *
     * @param sectionId идентификатор секции.
     * @return информация об указанной секции или null если такая секция отсутствует в данной секции.
     */
    public Section findSectionById(final String sectionId) {
        for (Section section : sections) {
            if (section.getId().equals(sectionId))
                return section;
            if (section instanceof CompositeSection) {
                final Section s = ((CompositeSection) section).findSectionById(sectionId);
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    /**
     * Возвращает список всех дочерних секций строго в той последовательсности в
     * которой они должны быть представлены в шаблоне отчета.
     *
     * @return список всех дочерних секций.
     */
    public List<Section> getSections() {
        return sections;
    }


    /**
     * @return Количество строк шаблона отведенных на описание данной секции.
     */
    public int getTemplateRowsCount() {
        int result = 0;
        for (GroupModel group : groups) {
            result += group.getStylesCount() * group.getRowsCount();
        }
        for (Section section : sections) {
            result += section.getTemplateRowsCount();
        }
        return result;
    }

    /**
     * Возвращает количество колонок отведенных на описание данной секции.
     *
     * @return количество колонок отведенных на описание секции.
     */
    public int getTemplateColumnsCount() {
        int result = 0;
        for (GroupModel group : groups) {
            result = Math.max(result, group.getColumnsCount());
        }
        for (Section section : sections) {
            result = Math.max(result, section.getTemplateColumnsCount());
        }
        return result;
    }


    /**
     * Выполняет глубокое копирование данной секции.
     *
     * @param target ссылка на модель отчета в который будет импортирована создаваемая копия секции.
     * Необходим для выполнения данной операции во избежание избыточного клонирования тех структур отчета, которые могут
     * использоваться разными секциями одного и того же отчета одновременно. К таковым структурам можно отнести:
     * <li> поставщики данных
     * <li> обработчики ошибок
     * <li> таблицы стилей ячеек секций.
     * @return глубокую копию данной секции.
     * @throws CloneNotSupportedException в случае проблем с клонированием какого-нибудь элемента секции.
     */
    @Override
    public Section cloneSection(final Report target) throws CloneNotSupportedException {
        final CompositeSection result = (CompositeSection) super.cloneSection(target);
        result.groups = new ArrayList<>();
        for (GroupModel gm : groups) {
            result.groups.add((GroupModel) gm.clone());
        }
        result.indentedColumns = new int[indentedColumns.length];
        System.arraycopy(indentedColumns, 0, result.indentedColumns, 0, indentedColumns.length);
        result.sections = new ArrayList<>();
        for (Section child : sections) {
            result.sections.add(child.cloneSection(target));
        }
        return result;
    }

    @Override
    public String toString() {
        return "[CompositeSection{id:" + getId() + ", groups:" + groups.size() + ", sections:" + sections.size() + "]";
    }
}
