package org.echosoft.framework.reports.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * Содержит часто используемые операции при работе с потоками.
 *
 * @author Anton Sharapov
 */
public class StreamUtil {

    /**
     * Размер буфера по умолчанию используемый при перекачке данных из потока в поток.
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    public static final InputStream EMPTY_INPUT_STREAM =
            new InputStream() {
                @Override
                public int read() {
                    return -1;
                }

                @Override
                public int read(final byte buf[], final int offset, final int length) {
                    return -1;
                }

                @Override
                public long skip(final long n) {
                    return 0;
                }
            };

    public static final OutputStream EMPTY_OUTPUT_STREAM =
            new OutputStream() {
                @Override
                public void write(final int b) {
                }

                @Override
                public void write(final byte[] buf) {
                }

                @Override
                public void write(final byte[] buf, final int offset, final int length) {
                }
            };

    public static final Reader EMPTY_READER =
            new Reader() {
                @Override
                public int read(final CharBuffer target) {
                    return -1;
                }

                @Override
                public int read() {
                    return -1;
                }

                @Override
                public int read(final char[] cbuf, final int off, final int len) {
                    return -1;
                }

                public long skip(final long n) {
                    return 0;
                }

                @Override
                public void close() {
                }
            };

    public static final Writer EMPTY_WRITER =
            new Writer() {
                @Override
                public void write(final int c) {
                }

                @Override
                public void write(final char[] cbuf) {
                }

                @Override
                public void write(final char[] cbuf, final int off, final int len) {
                }

                @Override
                public void write(final String str) {
                }

                @Override
                public void write(final String str, final int off, final int len) {
                }

                @Override
                public Writer append(final CharSequence csq) {
                    return this;
                }

                @Override
                public Writer append(final CharSequence csq, final int start, final int end) {
                    return this;
                }

                @Override
                public Writer append(final char c) {
                    return this;
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() {
                }
            };


    /**
     * Читает содержимое входного потока и помещает его в выходной поток.
     *
     * @param in  входной поток некоторых бинарных данных.
     * @param out выходной поток некоторых бинарных данных.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static void pipeData(final InputStream in, final OutputStream out) throws IOException {
        pipeData(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Читает содержимое входного потока и помещает его в выходной поток.
     *
     * @param in         входной поток некоторых бинарных данных.
     * @param out        выходной поток некоторых бинарных данных.
     * @param bufferSize размер буфера используемого для перекачки данных из потока на чтение в поток на запись.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static void pipeData(final InputStream in, final OutputStream out, final int bufferSize) throws IOException {
        final byte[] buf = new byte[bufferSize];
        int size;
        while ((size = in.read(buf)) > 0) {
            out.write(buf, 0, size);
        }
    }

    /**
     * Читает содержимое входного потока и помещает его в выходной поток.
     *
     * @param in  входной поток некоторых символьных данных.
     * @param out выходной поток некоторых символьных данных.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static void pipeData(final Reader in, final Writer out) throws IOException {
        pipeData(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Читает содержимое входного потока и помещает его в выходной поток.
     *
     * @param in         входной поток некоторых символьных данных.
     * @param out        выходной поток некоторых символьных данных.
     * @param bufferSize размер буфера используемого для перекачки данных из потока на чтение в поток на запись.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static void pipeData(final Reader in, final Writer out, final int bufferSize) throws IOException {
        final char[] buf = new char[bufferSize];
        int size;
        while ((size = in.read(buf)) > 0) {
            out.write(buf, 0, size);
        }
    }

    /**
     * Читает содержимое потока в указанный массив байт.
     *
     * @param in  входной поток.
     * @param buf буфер куда помещается прочитанные из потока байты..
     * @return реальное количество прочитанных байт. Может изменяться в диапазоне от 0 до N, где N - размер массива.
     * Если возвращаемое методом значение меньшее чем N значит поток был исчерпан полностью,
     * в противном случае - возможно (!) в потоке есть еще непрочитанное нами содержимое.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static int readFromStream(final InputStream in, final byte[] buf) throws IOException {
        final int length = buf.length;
        int readed = 0;
        for (; ; ) {
            final int n = in.read(buf, readed, length - readed);
            if (n < 0)
                return readed;
            readed += n;
            if (readed >= length)
                return readed;
        }
    }

    /**
     * Читает содержимое потока в указанный массив символов.
     *
     * @param in  входной поток.
     * @param buf буфер куда помещается прочитанные из потока байты..
     * @return реальное количество прочитанных байт. Может изменяться в диапазоне от 0 до N, где N - размер массива.
     * Если возвращаемое методом значение меньшее чем N значит поток был исчерпан полностью,
     * в противном случае - возможно (!) в потоке есть еще непрочитанное нами содержимое.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static int readFromReader(final Reader in, final char[] buf) throws IOException {
        final int length = buf.length;
        int readed = 0;
        for (; ; ) {
            final int n = in.read(buf, readed, length - readed);
            if (n < 0)
                return readed;
            readed += n;
            if (readed >= length)
                return readed;
        }
    }

    /**
     * Читает <u>все</u> содержимое потока в массив байт.
     *
     * @param in входной поток данных.
     * @return массив байт что были прочитаны из потока.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static byte[] streamToBytes(final InputStream in) throws IOException {
        return streamToBytes(in, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Читает <u>все</u> содержимое потока в массив байт.
     *
     * @param in         входной поток данных.
     * @param bufferSize размер буфера используемого для чтения данных из потока.
     * @return массив байт что были прочитаны из потока.
     * @throws IOException в случае возникновения каких-либо ошибок ввода-вывода.
     */
    public static byte[] streamToBytes(final InputStream in, final int bufferSize) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
        final byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        for (int size = in.read(buf); size > 0; size = in.read(buf)) {
            out.write(buf, 0, size);
        }
        return out.toByteArray();
    }
}
