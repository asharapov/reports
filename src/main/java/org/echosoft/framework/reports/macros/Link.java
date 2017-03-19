package org.echosoft.framework.reports.macros;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.echosoft.framework.reports.processor.ExecutionContext;

/**
 * <p></p>Используется для генерации гиперссылок в ячейках заданного листа.</p>
 * Макрос требует для работы следующие аргументы (разделяются запятой):
 * <ol>
 *     <li>Строка с ссылкой на внешний ресурс (веб, эл. почта, файл) - обязательно</li>
 *     <li>Лейбл для ссылки который будет показываться пользователю - опционально</li>
 * </ol>
 * <p>Примеры использования в ячейках:
 * <ol>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=link(http://kremlin.ru)</code></span>
 *  <p>Показывает в текущей ячейке ссылку на сайт "http://kremlin.ru". В ячейке будет продублирона эта ссылка.</p>
 *  </li>
 *  <li> <span style="border:1px solid black;padding:6px"><code>$M=link(http://kremlin.ru, Москва. Кремль)</code></span>
 *  <p>Показывает в текущей ячейке ссылку на сайт "http://kremlin.ru" и при этом в ячейке будет написано "Москва. Кремль"</p>
 *  </li>
 * </ol>
 * </p>
 *
 * @author Anton Sharapov
 */
public class Link implements Macros {

    @Override
    public void call(final ExecutionContext ectx, final String arg) {
        if (arg == null) {
            ectx.cell.setCellType(CellType.BLANK);
            return;
        }
        final int s = arg.indexOf(',');
        final String ref = s >= 0 ? arg.substring(0, s).trim() : arg.trim();
        final String label = s >= 0 ? arg.substring(s + 1).trim() : "";
        if (ref.isEmpty()) {
            ectx.cell.setCellType(CellType.BLANK);
            return;
        }

        final Hyperlink link;
        try {
            final URI uri = new URI(ref);
            final String scheme = uri.getScheme();
            if (scheme == null) {
                link = ectx.creationHelper.createHyperlink(HyperlinkType.FILE);
            } else
            if ("mailto".equals(scheme.toLowerCase())) {
                link = ectx.creationHelper.createHyperlink(HyperlinkType.EMAIL);
            } else {
                link = ectx.creationHelper.createHyperlink(HyperlinkType.URL);
            }
        } catch (URISyntaxException e) {
            // плохая ссылка, отрисовываем просто текст.
            ectx.cell.setCellValue(ref);
            return;
        }

        link.setAddress(ref);
        if (!label.isEmpty()) {
            link.setLabel(label);
            ectx.cell.setCellValue(label);
        } else {
            ectx.cell.setCellValue(ref);
        }
        ectx.cell.setHyperlink(link);
    }

}
