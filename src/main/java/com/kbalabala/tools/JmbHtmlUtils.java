package com.kbalabala.tools;

import jodd.util.StringPool;

import java.util.Map;
import static com.kbalabala.tools.Constants.*;

/**
 * HTML相关公共类
 * <p>
 *  <ol>
 *   <li>根据base路径和参数列表生成Html Url{@link com.kbalabala.tools.JmbHtmlUtils#toFormalUrl(String base, Map params, String dateFormat)}</li>
 *  </ol>
 * </p>
 * @author kevin
 * @since  2015-4-20
 */
public class JmbHtmlUtils {

    /**
     * 根据base路径和参数列表生成Html Url
     * @param base
     * @param params
     * @param dateFormat
     * @return
     */
    public static String toFormalUrl(String base, Map<String, Object> params, String dateFormat) {
        StringBuilder sb =
                new StringBuilder(base == null ? STRING_BLANK : base)
                        .append(StringPool.QUESTION_MARK)
                        .append(JmbStringUtils.joinPairs(params, dateFormat, StringPool.EQUALS, StringPool.AMPERSAND));

        return sb.toString();
    }
}
