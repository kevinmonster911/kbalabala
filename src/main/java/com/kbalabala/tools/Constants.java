package com.kbalabala.tools;

import java.nio.charset.Charset;

/**
 * Created by kevin on 15-4-17.
 */
public class Constants {

    public static final String CHAR_SEPARATOR_DOT = ".";

    public static final String STRING_BLANK = "";

    public static final String NUMBER_ZERO = "0";
    public static final String NUMBER_ZERO_2DECIMAL = "0.00";


    public static final String LINE_FEED = System.lineSeparator();


    // ############### time ################
    public static final int ONE_DAY = 60*60*24;

    public static final String BOOLEAN_TRUE = "true";
    public static final String BOOLEAN_FALSE = "false";

    public static final String CHARSET_UTF8 = "UTF-8";
    public static final Charset CS_CHARSET_UTF8 = Charset.forName(CHARSET_UTF8);
}
