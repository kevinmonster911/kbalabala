package com.kbalabala.tools;

import jodd.datetime.JDateTime;

import java.util.Date;

/**
 * 积木盒子字符串公共类
 * <p>
 *  <ol>
 *   <li>用'****'替换从start开始到end（不包含）之间的字符串{@link com.kbalabala.tools.JmbStringUtils#mask4Stars(String, int, int)}</li>
 *   <li>用'******'替换从start开始到end（不包含）之间的字符串{@link com.kbalabala.tools.JmbStringUtils#mask4Stars(String, int, int)}</li>
 *   <li>用特定字符串替换从start开始到end（不包含）之间的字符串{@link com.kbalabala.tools.JmbStringUtils#replaceInRange(String origin, int start, int end, String replacement)}</li>
 *  </ol>
 * </p>
 * @author kevin
 * @since  2015-4-20
 *
 */
public class JmbDateUtils {

    public static final String FORMAT_YYYY_MM_DD = "YYYY-MM-DD";
    public static final String FORMAT_YYYY_MM_DD_HHMMSS = "YYYY-MM-DD hh:mm:ss";

    public static JDateTime joddDate(Date date) {
        return new JDateTime(date);
    }

    public static String toYYYYMMDD(Date date) {
        return toString(date, FORMAT_YYYY_MM_DD);
    }

    public static Date fromYYYYMMDD(String date) {
        return toDate(date, FORMAT_YYYY_MM_DD);
    }

    public static String toYYYYMMDDhhmmss(Date date) {
        return toString(date, FORMAT_YYYY_MM_DD_HHMMSS);
    }

    public static Date fromYYYYMMDDhhmmss(String date) {
        return toDate(date, FORMAT_YYYY_MM_DD_HHMMSS);
    }

    public static String toString(Date date, String format){
        return joddDate(date).toString(format);
    }

    public static Date toDate(String date, String format){
        return new JDateTime(date, format).convertToDate();
    }

    public static Date now(){
        return new Date();
    }
}
