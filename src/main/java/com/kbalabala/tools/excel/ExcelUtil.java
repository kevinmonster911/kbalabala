package com.kbalabala.tools.excel;

import com.kbalabala.tools.JmbNumberUtils;
import com.kbalabala.tools.JmbStringUtils;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The <code>ExcelUtil</code>
 */
public class ExcelUtil {

    public static final String TO_PERCENT_ACCURACY = "ToPercentAccuracy";
    public static final String TO_DECIMAL_2 = "ToDecimal2";
    public static final String DATE_MODEL = "yyyy-MM-dd";
    public static final String DATETIME_MODEL = "yyyy-MM-dd HH:mm:ss";

    /**
     * 用来验证excel与Vo中的类型是否一致 <br>
     */
    private static Map<Class<?>, Integer[]> validateMap = new HashMap<Class<?>, Integer[]>();

    static {
        validateMap.put(String[].class, new Integer[]{Cell.CELL_TYPE_STRING});
        validateMap.put(Double[].class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(String.class, new Integer[]{Cell.CELL_TYPE_STRING});
        validateMap.put(Double.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Date.class, new Integer[]{Cell.CELL_TYPE_NUMERIC, Cell.CELL_TYPE_STRING});
        validateMap.put(Integer.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Float.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Long.class, new Integer[]{Cell.CELL_TYPE_NUMERIC});
        validateMap.put(Boolean.class, new Integer[]{Cell.CELL_TYPE_BOOLEAN});
    }

    /**
     * 获取cell类型的文字描述
     *
     * @param cellType <pre>
     *                 Cell.CELL_TYPE_BLANK
     *                 Cell.CELL_TYPE_BOOLEAN
     *                 Cell.CELL_TYPE_ERROR
     *                 Cell.CELL_TYPE_FORMULA
     *                 Cell.CELL_TYPE_NUMERIC
     *                 Cell.CELL_TYPE_STRING
     *                 </pre>
     * @return
     */
    private static String getCellTypeByInt(int cellType) {
        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                return "Null type";
            case Cell.CELL_TYPE_BOOLEAN:
                return "Boolean type";
            case Cell.CELL_TYPE_ERROR:
                return "Error type";
            case Cell.CELL_TYPE_FORMULA:
                return "Formula type";
            case Cell.CELL_TYPE_NUMERIC:
                return "Numeric type";
            case Cell.CELL_TYPE_STRING:
                return "String type";
            default:
                return "Unknown type";
        }
    }

    /**
     * 获取单元格值
     *
     * @param cell
     * @return
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null
                || (cell.getCellType() == Cell.CELL_TYPE_STRING && JmbStringUtils.isBlank(cell
                .getStringCellValue()))) {
            return null;
        }
        int cellType = cell.getCellType();
        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                return null;
        }
    }

    /**
     * 这是一个通用的方法，利用了JAVA的反射机制，可以将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个个sheet
     *
     * @param <T>
     * @param headers
     * @param dataset
     * @param out
     */
    public static <T> void exportExcel(String[] headers, Collection<T> dataset, OutputStream out) {
        exportExcel(headers, dataset, out, null);
    }

    public static <T> void exportExcel(String[] headers, String[] footers, Collection<T> dataset, OutputStream out) {
        exportExcel(headers, footers, dataset, out, "yyyy-MM-dd");
    }

    public static <T> void exportExcel(String[] headers, Collection<T> dataset, OutputStream out,String sheetName,String title){
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(sheetName);

        write2Sheet(sheet, headers, null, dataset, null,title);
        try {
            workbook.write(out);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * 这是一个通用的方法，利用了JAVA的反射机制，可以将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于单个个sheet
     *
     * @param <T>
     * @param headers
     * @param dataset
     * @param out
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyy-MM-dd"
     */
    public static <T> void exportExcel(String[] headers, Collection<T> dataset, OutputStream out,
                                       String pattern) {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();

        write2Sheet(sheet, headers, null, dataset, pattern,null);
        try {
            workbook.write(out);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    public static <T> void exportExcel(String[] headers, String[] footers, Collection<T> dataset, OutputStream out,
                                       String pattern) {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet();

        write2Sheet(sheet, headers, footers, dataset, pattern,null);
        try {
            workbook.write(out);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * 这是一个通用的方法，利用了JAVA的反射机制，可以将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     *
     * @param <T>
     * @param sheets
     * @param out
     */
    public static <T> void exportExcel(List<ExcelSheet<T>> sheets, OutputStream out) {
        exportExcel(sheets, out, null);
    }

    /**
     * 这是一个通用的方法，利用了JAVA的反射机制，可以将放置在JAVA集合中并且符号一定条件的数据以EXCEL 的形式输出到指定IO设备上<br>
     * 用于多个sheet
     *
     * @param <T>
     * @param sheets
     * @param out
     * @param pattern
     */
    public static <T> void exportExcel(List<ExcelSheet<T>> sheets, OutputStream out, String pattern) {
        if (CollectionUtils.isEmpty(sheets)) {
            return;
        }
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        for (ExcelSheet<T> sheet : sheets) {
            // 生成一个表格
            HSSFSheet hssfSheet = workbook.createSheet(sheet.getSheetName());
            writeToSheet(hssfSheet, sheet.getHeaders(), null, sheet.getDataset(), pattern);
        }
        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每个sheet的写入
     *
     * @param sheet
     * @param headers
     * @param footers
     * @param dataset
     * @param pattern
     */
    private static <T> void write2Sheet(HSSFSheet sheet, String[] headers, String[] footers, Collection<T> dataset,
                                        String pattern,String title) {

        HSSFCellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
        cellStyleHeader.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellStyleHeader.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellStyleHeader.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellStyleHeader.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        cellStyleHeader.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        HSSFFont fontHeader = sheet.getWorkbook().createFont();
        fontHeader.setFontName("宋体 (正文)");
        fontHeader.setFontHeightInPoints((short)10);
        fontHeader.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        cellStyleHeader.setFont(fontHeader);


        HSSFCellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框

        HSSFFont font = sheet.getWorkbook().createFont();
        font.setFontName("宋体 (正文)");
        font.setFontHeightInPoints((short)10);

        cellStyle.setFont(font);

        HSSFCellStyle cellDigitStyle = sheet.getWorkbook().createCellStyle();
        cellDigitStyle.setDataFormat((short)4);
        cellDigitStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellDigitStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellDigitStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellDigitStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        cellDigitStyle.setFont(font);

        HSSFCellStyle cellPercentStyle = sheet.getWorkbook().createCellStyle();
        cellPercentStyle.setDataFormat((short)10);
        cellPercentStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellPercentStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellPercentStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellPercentStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        cellPercentStyle.setFont(font);

        HSSFRow row = sheet.createRow(0);
        if(null != title) {
            HSSFCellStyle cellStyleTitle = sheet.getWorkbook().createCellStyle();

            HSSFFont fontTitle = sheet.getWorkbook().createFont();
            fontTitle.setFontName("宋体 (正文)");
            fontTitle.setFontHeightInPoints((short)18);
            fontTitle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

            HSSFCell cell = row.createCell(0);
            HSSFRichTextString text = new HSSFRichTextString(title);

            cellStyleTitle.setFont(fontTitle);
            cell.setCellStyle(cellStyleTitle);
            cell.setCellValue(text);

        }

        row = sheet.createRow(1);

        // 产生表格标题行
        row = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);

            cell.setCellStyle(cellStyleHeader);
            cell.setCellValue(text);
        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 2;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            T t = (T) it.next();
            try {
                List<FieldForSortting> fields = sortFieldByAnno(t.getClass());
                int cellNum = 0;
                for (int i = 0; i < fields.size(); i++) {
                    HSSFCell cell = row.createCell(cellNum);
                    cell.setCellStyle(cellStyle);
                    Field field = fields.get(i).getField();
                    TypeResolver resolver = getResolver(field);
                    field.setAccessible(true);
                    Object value = field.get(t);
                    if(resolver != null && value != null) {
                        value = resolver.resolve(value);
                    }

                    String textValue = null;
                    if (value instanceof Integer) {
                        int intValue = (Integer) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(intValue);
                    } else if (value instanceof Float) {
                        float fValue = (Float) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(fValue);
                    } else if (value instanceof Double) {
                        double dValue = (Double) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(dValue);
                    } else if (value instanceof Long) {
                        long longValue = (Long) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(longValue);
                    } else if(value instanceof BigDecimal){
                        BigDecimal dValue = (BigDecimal)value;
                        String fieldPattern = getFieldPattern(field);
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);

                        switch (fieldPattern) {
                            case TO_DECIMAL_2:
                                cell.setCellStyle(cellDigitStyle);
                                cell.setCellValue(Double.parseDouble(JmbNumberUtils.toDecimal2Pos(dValue)));
                                break;
                            case TO_PERCENT_ACCURACY:
                                cell.setCellStyle(cellPercentStyle);
                                cell.setCellValue(dValue.doubleValue());
                                break;
                            default:
                                cell.setCellValue(dValue.doubleValue());
                        }

                    }else if (value instanceof Boolean) {
                        boolean bValue = (Boolean) value;
                        cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
                        cell.setCellValue(bValue);
                    } else if (value instanceof Date) {
                        Date date = (Date) value;
                        String fieldPattern = getFieldPattern(field);
                        SimpleDateFormat sdf;
                        if(JmbStringUtils.isNotEmpty(fieldPattern)) {
                            sdf = new SimpleDateFormat(fieldPattern);
                        } else {
                            sdf = new SimpleDateFormat(pattern);
                        }
                        textValue = sdf.format(date);
                    } else {
                        // 其它数据类型都当作字符串简单处理
                        String empty = "";
                        ExcelCell anno = field.getAnnotation(ExcelCell.class);
                        if (anno != null) {
                            empty = anno.defaultValue();
                        }
                        textValue = value == null ? empty : value.toString();
                    }
                    if (textValue != null) {
                        HSSFRichTextString richString = new HSSFRichTextString(textValue);
                        cell.setCellValue(richString);
                    }

                    cellNum++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(footers != null){
            index++;
            row = sheet.createRow(index);
            for (int i = 0; i < footers.length; i++) {
                HSSFCell cell = row.createCell(i);
                HSSFRichTextString text = new HSSFRichTextString(footers[i]);
                cell.setCellValue(text);
            }
        }

        // 设定自动宽度
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    /**
     * 每个sheet的写入,去掉顶头的空行
     *
     * @param sheet
     * @param headers
     * @param footers
     * @param dataset
     * @param pattern
     */
    private static <T> void writeToSheet(HSSFSheet sheet, String[] headers, String[] footers, Collection<T> dataset,
                                        String pattern) {

        HSSFCellStyle cellStyleHeader = sheet.getWorkbook().createCellStyle();
        cellStyleHeader.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellStyleHeader.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellStyleHeader.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellStyleHeader.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        cellStyleHeader.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        HSSFFont fontHeader = sheet.getWorkbook().createFont();
        fontHeader.setFontName("宋体 (正文)");
        fontHeader.setFontHeightInPoints((short)10);
        fontHeader.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        cellStyleHeader.setFont(fontHeader);


        HSSFCellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框

        HSSFFont font = sheet.getWorkbook().createFont();
        font.setFontName("宋体 (正文)");
        font.setFontHeightInPoints((short)10);

        cellStyle.setFont(font);

        HSSFCellStyle cellDigitStyle = sheet.getWorkbook().createCellStyle();
        cellDigitStyle.setDataFormat((short)4);
        cellDigitStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellDigitStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellDigitStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellDigitStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        cellDigitStyle.setFont(font);

        HSSFCellStyle cellPercentStyle = sheet.getWorkbook().createCellStyle();
        cellPercentStyle.setDataFormat((short)10);
        cellPercentStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellPercentStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellPercentStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellPercentStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        cellPercentStyle.setFont(font);

        HSSFRow row = sheet.createRow(0);
        // 产生表格标题行
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);

            cell.setCellStyle(cellStyleHeader);
            cell.setCellValue(text);
        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            T t = (T) it.next();
            try {
                List<FieldForSortting> fields = sortFieldByAnno(t.getClass());
                int cellNum = 0;
                for (int i = 0; i < fields.size(); i++) {
                    HSSFCell cell = row.createCell(cellNum);
                    cell.setCellStyle(cellStyle);
                    Field field = fields.get(i).getField();
                    TypeResolver resolver = getResolver(field);
                    field.setAccessible(true);
                    Object value = field.get(t);
                    if(resolver != null && value != null) {
                        value = resolver.resolve(value);
                    }

                    String textValue = null;
                    if (value instanceof Integer) {
                        int intValue = (Integer) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(intValue);
                    } else if (value instanceof Float) {
                        float fValue = (Float) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(fValue);
                    } else if (value instanceof Double) {
                        double dValue = (Double) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(dValue);
                    } else if (value instanceof Long) {
                        long longValue = (Long) value;
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cell.setCellValue(longValue);
                    } else if(value instanceof BigDecimal){
                        BigDecimal dValue = (BigDecimal)value;
                        String fieldPattern = getFieldPattern(field);
                        cell.setCellType(Cell.CELL_TYPE_NUMERIC);

                        switch (fieldPattern) {
                            case TO_DECIMAL_2:
                                cell.setCellStyle(cellDigitStyle);
                                cell.setCellValue(Double.parseDouble(JmbNumberUtils.toDecimal2Pos(dValue)));
                                break;
                            case TO_PERCENT_ACCURACY:
                                cell.setCellStyle(cellPercentStyle);
                                cell.setCellValue(dValue.doubleValue());
                                break;
                            default:
                                cell.setCellValue(dValue.doubleValue());
                        }

                    }else if (value instanceof Boolean) {
                        boolean bValue = (Boolean) value;
                        cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
                        cell.setCellValue(bValue);
                    } else if (value instanceof Date) {
                        Date date = (Date) value;
                        String fieldPattern = getFieldPattern(field);
                        SimpleDateFormat sdf;
                        if(JmbStringUtils.isNotEmpty(fieldPattern)) {
                            sdf = new SimpleDateFormat(fieldPattern);
                        } else {
                            sdf = new SimpleDateFormat(pattern);
                        }
                        textValue = sdf.format(date);
                    } else {
                        // 其它数据类型都当作字符串简单处理
                        String empty = "";
                        ExcelCell anno = field.getAnnotation(ExcelCell.class);
                        if (anno != null) {
                            empty = anno.defaultValue();
                        }
                        textValue = value == null ? empty : value.toString();
                    }
                    if (textValue != null) {
                        HSSFRichTextString richString = new HSSFRichTextString(textValue);
                        cell.setCellValue(richString);
                    }

                    cellNum++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(footers != null){
            index++;
            row = sheet.createRow(index);
            for (int i = 0; i < footers.length; i++) {
                HSSFCell cell = row.createCell(i);
                HSSFRichTextString text = new HSSFRichTextString(footers[i]);
                cell.setCellValue(text);
            }
        }

        // 设定自动宽度
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    /**
     * 把Excel的数据封装成voList
     *
     * @param clazz
     * @param inputStream
     * @param pattern
     * @param logs
     * @param arrayCount
     * @return voList
     * @throws RuntimeException
     */
    public static <T> Collection<T> importExcel(Class<T> clazz, InputStream inputStream, String pattern,
                                                ExcelLogs logs, Integer... arrayCount) {
        HSSFWorkbook workBook = null;
        try {
            workBook = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<T> list = new ArrayList<T>();
        HSSFSheet sheet = workBook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        try {
            List<ExcelLog> logList = new ArrayList<ExcelLog>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getRowNum() == 0) {
                    continue;
                }
                // 整行都空，就跳过
                boolean allRowIsNull = true;
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Object cellValue = getCellValue(cellIterator.next());
                    if (cellValue != null) {
                        allRowIsNull = false;
                        break;
                    }
                }
                if (allRowIsNull) {
                    System.out.println("Excel row " + row.getRowNum() + " all row value is null!");
                    continue;
                }
                T t = clazz.newInstance();
                int arrayIndex = 0;// 标识当前第几个数组了
                int cellIndex = 0;// 标识当前读到这一行的第几个cell了
                List<FieldForSortting> fields = sortFieldByAnno(clazz);
                StringBuilder log = new StringBuilder();
                for (FieldForSortting ffs : fields) {
                    Field field = ffs.getField();
                    field.setAccessible(true);

                    Cell cell = row.getCell(cellIndex);
                    String errMsg = validateCell(cell, field, cellIndex);
                    if (JmbStringUtils.isBlank(errMsg)) {
                        Object value = null;
                        // 处理特殊情况,Excel中的String,转换成Bean的Date
                        if (field.getType().equals(Date.class)
                                && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                            Object strDate = getCellValue(cell);
                            try {
                                value = new SimpleDateFormat(pattern).parse(strDate.toString());
                            } catch (ParseException e) {

                                errMsg = MessageFormat.format(
                                        "the cell [{0}] can not be converted to a date ",
                                        CellReference.convertNumToColString(cell.getColumnIndex()));
                            }
                        } else {
                            value = getCellValue(cell);
                            // 处理特殊情况,excel的value为String,且bean中为其他,且defaultValue不为空,那就=defaultValue
                            ExcelCell annoCell = field.getAnnotation(ExcelCell.class);
                            if (value instanceof String && !field.getType().equals(String.class)
                                    && JmbStringUtils.isNotBlank(annoCell.defaultValue())) {
                                value = annoCell.defaultValue();
                            }
                        }
                        field.set(t, value);
                    }
                    if (JmbStringUtils.isNotBlank(errMsg)) {
                        log.append(errMsg);
                        log.append(";");
                        logs.setHasError(true);
                    }
                    cellIndex++;
                }

                list.add(t);
                logList.add(new ExcelLog(t, log.toString(), row.getRowNum() + 1));
            }
            logs.setLogList(logList);
        } catch (InstantiationException e) {
            throw new RuntimeException(MessageFormat.format("can not instance class:{0}",
                    clazz.getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(MessageFormat.format("can not instance class:{0}",
                    clazz.getSimpleName()), e);
        }
        return list;
    }

    /**
     * 验证cell是否正确
     *
     * @param cell
     * @param field
     * @param cellNum
     * @return
     */
    private static String validateCell(Cell cell, Field field, int cellNum) {
        String columnName = CellReference.convertNumToColString(cellNum);
        String result = null;
        Integer[] integers = validateMap.get(field.getType());
        if (integers == null) {
            result = MessageFormat.format("Unsupported type [{0}]", field.getType().getSimpleName());
            return result;
        }
        ExcelCell annoCell = field.getAnnotation(ExcelCell.class);
        if (cell == null
                || (cell.getCellType() == Cell.CELL_TYPE_STRING && JmbStringUtils.isBlank(cell
                .getStringCellValue()))) {
            if (annoCell != null && annoCell.valid().allowNull() == false) {
                result = MessageFormat.format("the cell [{0}] can not null", columnName);
            }
            ;
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK && annoCell.valid().allowNull()) {
            return result;
        } else {
            List<Integer> cellTypes = Arrays.asList(integers);

            if (!(cellTypes.contains(cell.getCellType())) || JmbStringUtils.isNotBlank(annoCell.defaultValue())
                    && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                StringBuilder strType = new StringBuilder();
                for (int i = 0; i < cellTypes.size(); i++) {
                    Integer intType = cellTypes.get(i);
                    strType.append(getCellTypeByInt(intType));
                    if (i != cellTypes.size() - 1) {
                        strType.append(",");
                    }
                }
                result = MessageFormat.format("the cell [{0}] type must [{1}]", columnName,
                        strType.toString());
            } else {
                // 类型符合验证,但值不在要求范围内的
                // String in
                if (annoCell.valid().in().length != 0 && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    String[] in = annoCell.valid().in();
                    String cellValue = cell.getStringCellValue();
                    boolean isIn = false;
                    for (String str : in) {
                        if (str.equals(cellValue)) {
                            isIn = true;
                        }
                    }
                    if (!isIn) {
                        result = MessageFormat.format("the cell [{0}] value must in {1}", columnName, in);
                    }
                }
                // 数字型
                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    double cellValue = cell.getNumericCellValue();
                    // 小于
                    if (!Double.isNaN(annoCell.valid().lt())) {
                        if (!(cellValue < annoCell.valid().lt())) {
                            result = MessageFormat.format("the cell [{0}] value must less than [{1}]",
                                    columnName, annoCell.valid().lt());
                        }
                    }
                    // 大于
                    if (!Double.isNaN(annoCell.valid().gt())) {
                        if (!(cellValue > annoCell.valid().gt())) {
                            result = MessageFormat.format("the cell [{0}] value must greater than [{1}]",
                                    columnName, annoCell.valid().gt());
                        }
                    }
                    // 小于等于
                    if (!Double.isNaN(annoCell.valid().le())) {
                        if (!(cellValue <= annoCell.valid().le())) {
                            result = MessageFormat.format(
                                    "the cell [{0}] value must less than or equal [{1}]", columnName,
                                    annoCell.valid().le());
                        }
                    }
                    // 大于等于
                    if (!Double.isNaN(annoCell.valid().ge())) {
                        if (!(cellValue >= annoCell.valid().ge())) {
                            result = MessageFormat.format(
                                    "the cell [{0}] value must greater than or equal [{1}]", columnName,
                                    annoCell.valid().ge());
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据annotation排序属性
     *
     * @param clazz
     * @return
     */
    private static List<FieldForSortting> sortFieldByAnno(Class<?> clazz) {
        Field[] fieldsArr = clazz.getDeclaredFields();
        List<FieldForSortting> fields = new ArrayList<FieldForSortting>();
        List<FieldForSortting> annoNullFields = new ArrayList<FieldForSortting>();
        for (Field field : fieldsArr) {
            ExcelCell ec = field.getAnnotation(ExcelCell.class);
            if (ec == null) {
                continue;
            }
            int id = ec.index();
            fields.add(new FieldForSortting(field, id));
        }
        fields.addAll(annoNullFields);
        sortByProperties(fields, true, false, "index");
        return fields;
    }

    @SuppressWarnings("unchecked")
    private static void sortByProperties(List<? extends Object> list, boolean isNullHigh, boolean isReversed,
                                         String... props) {
        if (CollectionUtils.isNotEmpty(list)) {
            Comparator<?> typeComp = ComparableComparator.getInstance();
            if (isNullHigh == true) {
                typeComp = ComparatorUtils.nullHighComparator(typeComp);
            } else {
                typeComp = ComparatorUtils.nullLowComparator(typeComp);
            }
            if (isReversed) {
                typeComp = ComparatorUtils.reversedComparator(typeComp);
            }

            List<Object> sortCols = new ArrayList<Object>();

            if (props != null) {
                for (String prop : props) {
                    sortCols.add(new BeanComparator(prop, typeComp));
                }
            }
            if (sortCols.size() > 0) {
                Comparator<Object> sortChain = new ComparatorChain(sortCols);
                Collections.sort(list, sortChain);
            }
        }
    }

    private static TypeResolver getResolver(Field field){
        if (field == null) return null;
        ExcelCell excelCell = field.getAnnotation(ExcelCell.class);
        TypeResolver resolver = null;
        if(TypeResolver.class.isAssignableFrom(excelCell.resolver())){
            try {
                resolver = (TypeResolver)excelCell.resolver().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return resolver;
    }

    private static String getFieldPattern(Field field){
        if (field == null) return null;
        ExcelCell excelCell = field.getAnnotation(ExcelCell.class);
        return excelCell.pattern();
    }
}
