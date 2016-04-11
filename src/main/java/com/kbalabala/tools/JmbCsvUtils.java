package com.kbalabala.tools;

import jodd.io.FileUtil;
import jodd.io.StreamUtil;
import jodd.util.CsvUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import static com.kbalabala.tools.Constants.*;

import static com.kbalabala.tools.Constants.CHARSET_UTF8;

/**
 * 基于Jodd csv的简单Csv Decode和Encode工具
 * 适合于csv文件较小且解析不频繁的场景，csv文件小于1万行均可使用。
 *
 * <p>
 *  <ol>
 *   <li>获取Csv文件迭代器{@link com.kbalabala.tools.JmbCsvUtils#fromCsv(String path)}</li>
 *   <li>获取Csv文件Writer{@link com.kbalabala.tools.JmbCsvUtils#toCsv(String path)}</li>
 *  </ol>
 * </p>
 * @author kevin
 * @since  2015-4-20
 */
public class JmbCsvUtils extends CsvUtil{

    /**
     * 获取Csv文件迭代器
     * @param path
     * @return
     * @throws IOException
     */
    public static CsvLineIterator fromCsv(String path) throws IOException {
        return fromCsv(Paths.get(path).toFile());
    }

    public static CsvLineIterator fromCsv(File file) throws IOException {
        String[] lines = FileUtil.readLines(file);
        return new CsvLineIterator(lines);
    }


    /**
     * 获取Csv文件Writer
     * @param path
     * @return
     * @throws IOException
     */
    public static CsvLineWriter toCsv(String path) {
        try {
            Files.createFile(Paths.get(path)); // create file
            return toCsv(Paths.get(path).toFile());
        } catch (IOException error) {
            return null;
        }
    }

    public static CsvLineWriter toCsv(File file) throws IOException {
        return new CsvLineWriter(file);
    }


    /**
     * 重写迭代器用于实现对于Csv行的解析封装和懒解析功能
     */
    public static class CsvLineIterator implements Iterator<String[]> {

        private String[] lines = null;

        private int current = -1;

        private int length = 0;

        public CsvLineIterator(String[] lines){
            this.lines = lines;
            this.length = lines == null ? 0 : lines.length - 1;
        }

        @Override
        public boolean hasNext() {
            return current < length;
        }

        @Override
        public String[] next() {
            if(current >= length) return null;
            current++;
            return toStringArray(lines[current]);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("do not support to remove item from iterator!");
        }

    }

    /**
     * Csv Writer用于生成csv文件中的行
     */
    public static class CsvLineWriter implements AutoCloseable {

        private Writer out;

        public CsvLineWriter(File file) throws IOException {
            out = new OutputStreamWriter(new FileOutputStream(file), CHARSET_UTF8);
        }

        public void writeLine(String[] line) throws IOException {
            out.write(JmbStringUtils.join(toCsvString(line), LINE_FEED));
        }

        @Override
        public void close() {
            try {
                out.flush(); // flush the rest parts in buffer
                StreamUtil.close(out);
            } catch (IOException e) {
                // do nothing for closing file
            }
        }
    }

}
