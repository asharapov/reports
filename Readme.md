[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.asharapov/spreadsheet-reports/badge.png)](https://search.maven.org/artifact/io.github.asharapov/spreadsheet-reports/)

# Reports

Еще одна библиотека для построения отчетов в формате документов Microsoft Excel.  
Основным ее отличием от аналогичных решений является ориентация на использование таких возможностей современных электронных таблиц как:
- формулы; 
- именованные регионы;
- группировки строк и столбцов; 
- условное форматирование.

Базовые возможности библиотеки продемонстрированы в ее тестах. Шаблоны демонстрационных примеров доступны в каталоге `src/test/resources/org/echosoft/framework/reports/test`.
Шаблон каждого отчета описывается в двух файлах с одинаковым именем но отличающимся расширением имени:
- .xml файл с описанием разметки будущего отчета и используемых источников данных
- .xlsx или .xls документ с описанием шаблона отчета
   
По итогам выполнения команды
```shell script
$ mvn clean test
```
В каталоге `target/reports` будут сформированы итоговые отчеты для существующих демонстрационных примеров. 
