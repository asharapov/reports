package org.echosoft.framework.reports.common.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Содержит методы, часто используемые при работе с DOM моделью документов XML.
 *
 * @author Andrey Ochirov
 * @author Anton Sharapov
 */
public class XMLUtil {

    private static final Properties SER_PROPS;

    static {
        SER_PROPS = new Properties();
        SER_PROPS.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        SER_PROPS.put(OutputKeys.INDENT, "yes");
        SER_PROPS.put(OutputKeys.METHOD, "xml");
        SER_PROPS.put("{http://xml.apache.org/xslt}indent-amount", "2");
    }

    private static class ElementsIterator implements Iterator<Element> {
        private final NodeList nodes;
        private final String ns;
        private int nextPos;
        private Element nextElement;

        private ElementsIterator(final NodeList nodes, final String ns) {
            this.nodes = nodes;
            this.ns = ns;
            this.nextPos = 0;
            this.nextElement = seekNext();
        }

        @Override
        public boolean hasNext() {
            return nextElement != null;
        }

        @Override
        public Element next() {
            if (nextElement == null)
                throw new NoSuchElementException();
            final Element result = nextElement;
            nextElement = seekNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("operation not supported");
        }

        private Element seekNext() {
            for (int i = nextPos, len = nodes.getLength(); i < len; i++) {
                final Node childNode = nodes.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE && (ns == null || ns.equals(childNode.getNamespaceURI()))) {
                    nextPos = i + 1;
                    return (Element) childNode;
                }
            }
            return null;
        }
    }

    private XMLUtil() {
    }

    /**
     * Загружает xml документ в виде DOM дерева.
     *
     * @param in входной поток данных содержащих свнедения о дереве.
     * @return прочитанный документ.
     */
    public static Document loadDocument(final InputStream in) throws IOException, ParserConfigurationException, SAXException {
        return loadDocument(in, false, null);
    }

    /**
     * Загружает xml документ в виде DOM дерева.
     *
     * @param in      входной поток данных содержащих свнедения о дереве.
     * @param nsAware включать или нет поддержку пространств имен.
     * @param schema  заполняется если требуется включить валидацию структуры документа согласно представленной схеме.
     * @return прочитанный документ.
     */
    public static Document loadDocument(final InputStream in, final boolean nsAware, final Schema schema) throws IOException, ParserConfigurationException, SAXException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(nsAware);
        final DocumentBuilder builder = dbf.newDocumentBuilder();
        final Document doc = builder.parse(new InputSource(in));
        if (schema != null) {
            final Validator validator = schema.newValidator();
            validator.validate(new DOMSource(doc));
        }
        return doc;
    }


    /**
     * Возвращает итератор по дочерним элементам дерева.
     *
     * @param parentNode элемент дерева по чьим дочерним узлам будет осуществляться поиск.
     */
    public static Iterable<Element> getChildElements(final Element parentNode) {
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return new ElementsIterator(parentNode.getChildNodes(), null);
            }
        };
    }

    /**
     * Возвращает итератор по дочерним элементам дерева.
     *
     * @param parentNode элемент дерева по чьим дочерним узлам будет осуществляться поиск.
     * @param ns         неймспейс чьи элементы нам интересны, если <code>null</code> то ограничений по имени неймспейса не будет.
     */
    public static Iterable<Element> getChildElements(final Element parentNode, final String ns) {
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return new ElementsIterator(parentNode.getChildNodes(), ns);
            }
        };
    }

    /**
     * Возвращает первый дочерний элемент с указанным именем.
     *
     * @param parentNode родительский элемент.
     * @param childName  имя искомого элемента.
     * @return первый найденный элемент или <code>null</code>.
     */
    public static Element getChildElement(final Element parentNode, final String childName) {
        return getChildElement(parentNode, null, childName);
    }

    /**
     * Возвращает первый дочерний элемент с указанным именем.
     *
     * @param parentNode родительский элемент.
     * @param ns         неймспейс чьи элементы нам интересны, если <code>null</code> то ограничений по имени неймспейса не будет.
     * @param childName  локальное имя искомого дочернего элемента.
     * @return первый найденный элемент или <code>null</code>.
     */
    public static Element getChildElement(final Element parentNode, final String ns, final String childName) {
        for (Node node = parentNode.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            if (ns != null && !ns.equals(node.getNamespaceURI()))
                continue;
            if (childName != null) {
                String ln = node.getLocalName();
                if (ln == null)
                    ln = node.getNodeName();
                if (!childName.equals(ln))
                    continue;
            }
            return (Element) node;
        }
        return null;
    }

    public static Element getNextSiblingElement(Node node, final String ns, final String siblingName) {
        while (true) {
            node = node.getNextSibling();
            if (node == null)
                return null;
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;
            if (ns != null && !ns.equals(node.getNamespaceURI()))
                continue;
            if (siblingName != null) {
                String ln = node.getLocalName();
                if (ln == null)
                    ln = node.getNodeName();
                if (!siblingName.equals(ln))
                    continue;
            }
            return (Element)node;
        }
    }


    /**
     * Возвращает текстовое значение для указанного узла дерева, предварительно склеив содержимое всех дочерних текстовых узлов дерева.
     *
     * @param node узел для которого возвращается текстовое значение.
     * @return искомый текст с обрезанными концевыми пробелами или <code>null</code>.
     */
    public static String getNodeText(final Node node) {
//        node.normalize();
        StringBuilder buf = null;
        for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
            final int type = n.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
                final String value = n.getNodeValue();
                if (value != null && value.length() > 0) {
                    if (buf == null)
                        buf = new StringBuilder(value.length());
                    buf.append(value);
                }
            }
        }
        return buf != null ? StringUtil.trim(buf.toString()) : null;
    }

    /**
     * Возвращает текстовое значение для указанного дочернего элемента.
     *
     * @param parentElement элемент относительно которого ищется дочерний элемент для которого, в свою очередь, вычисляется его значение.
     * @param ns            неймспейс дочернего элемента, если <code>null</code> то ограничений по имени неймспейса не будет.
     * @param childName     имя дочернего элемента.
     * @return текстовое значение найденного дочернего элемента или <code>null</code>
     */
    public static String getChildNodeText(final Element parentElement, final String ns, final String childName) {
        final Element childElement = getChildElement(parentElement, ns, childName);
        return childElement != null ? getNodeText(childElement) : null;
    }


    /**
     * Select node list what matches given xpath query
     *
     * @param doc        xml document
     * @param expression xpath query
     * @return nodes which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static NodeList query(final Document doc, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (NodeList) xpath.evaluate(expression, doc.getDocumentElement(), XPathConstants.NODESET);
    }

    /**
     * Select node list what matches given xpath query
     *
     * @param node       xml node
     * @param expression xpath query
     * @return nodes which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static NodeList query(final Node node, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (NodeList) xpath.evaluate(expression, node, XPathConstants.NODESET);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param doc        xml document
     * @param expression xpath query
     * @return first element which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static Element queryElement(final Document doc, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (Element) xpath.evaluate(expression, doc, XPathConstants.NODE);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param node       xml node
     * @param expression xpath query
     * @return first element which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static Element queryElement(final Node node, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (Element) xpath.evaluate(expression, node, XPathConstants.NODE);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param doc   xml document
     * @param xpath xpath query
     * @return text under element which confirms given xpath query
     * @throws XPathExpressionException in case of any errors.
     */
    public static String queryText(final Document doc, final String xpath) throws XPathExpressionException {
        return queryText(doc.getDocumentElement(), xpath);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param node       xml node
     * @param expression xpath query
     * @return text under element which confirms given xpath query
     * @throws XPathExpressionException in case of any errors.
     */
    public static String queryText(final Node node, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final Node n = (Node) xpath.evaluate(expression, node, XPathConstants.NODE);
        if (n == null)
            return null;
        return (n.getNodeType() == Node.TEXT_NODE) ? n.getNodeValue() : getNodeText(n);
    }


    /**
     * Create new DOM document.
     *
     * @return new xml DOM document.
     * @throws ParserConfigurationException in case of any errors.
     */
    public static Document createDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
        dFactory.setNamespaceAware(true);
        final DocumentBuilder docBuilder = dFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }


    /**
     * Create new DOM document from XMl string.
     *
     * @param xml text of the serialized XML document.
     * @return new XML DOC document.
     * @throws ParserConfigurationException in case of parsing errors.
     * @throws IOException                  in case of io errors.
     * @throws SAXException                 in case errors.
     */
    public static Document createDocument(final String xml) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
        dFactory.setNamespaceAware(true);
        final DocumentBuilder docBuilder = dFactory.newDocumentBuilder();
        final InputSource xmlSourse = new InputSource(new StringReader(xml));
        return docBuilder.parse(xmlSourse);
    }


    /**
     * Serialize document to string.
     *
     * @param node node which must be serialized.
     * @return serialized form of the node.
     * @throws TransformerException in case of any errors.
     */
    public static String serialize(final Node node) throws TransformerException {
        final Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperties(SER_PROPS);
        final StringWriter writer = new StringWriter(1024);
        serializer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }


    /**
     * Serialize document to the output file.
     *
     * @param node       node which must be serialized.
     * @param outputFile output file.
     * @throws TransformerException in case of any errors.
     * @throws IOException          in case any io errors.
     */
    public static void serialize(final Node node, final File outputFile) throws TransformerException, IOException {
        final Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperties(SER_PROPS);
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            serializer.transform(new DOMSource(node), new StreamResult(out));
        }
    }


    /**
     * Process the source DOM tree to the output DOM tree
     *
     * @param xmlDoc model DOM tree
     * @param xslDoc template DOM tree
     * @return produced DOM tree.
     * @throws TransformerException         in case of any errors due documents transformation
     * @throws ParserConfigurationException in case of any errors due documents parsing
     */
    public static Document apply(final Document xmlDoc, final Document xslDoc) throws TransformerException, ParserConfigurationException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer(new DOMSource(xslDoc));
        final Document targetDoc = createDocument();
        transformer.transform(new DOMSource(xmlDoc), new DOMResult(targetDoc));
        return targetDoc;
    }


    /**
     * Process the source document to the output document
     *
     * @param xmlFile  file with model DOM tree
     * @param xslFile  file with template DOM tree
     * @param htmlFile file with produced DOM tree.
     * @throws TransformerException in case of any errors due documents transformation
     */
    public static void apply(final File xmlFile, final File xslFile, final File htmlFile) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xslFile));
        transformer.transform(new StreamSource(xmlFile), new StreamResult(htmlFile));
    }
}
