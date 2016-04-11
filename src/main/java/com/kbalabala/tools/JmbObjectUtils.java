package com.kbalabala.tools;

import jodd.util.ObjectUtil;

import java.util.Date;
import static com.kbalabala.tools.Constants.*;

/**
 * 对象公共类
 * <p>
 *  <ol>
 *   <li>转换对象到字符串{@link com.kbalabala.tools.JmbObjectUtils#toString(Object obj, String dateFormat)}</li>
 *  </ol>
 * </p>
 * @author kevin
 * @since  2015-4-20
 */
public class JmbObjectUtils extends ObjectUtil {

    /**
     * 转换对象到字符串
     * @param obj
     * @param dateFormat
     * @return
     */
    public static String toString(Object obj, String dateFormat) {

        if (obj instanceof Date) {
            return JmbDateUtils.toString((Date) obj, dateFormat);
        } else if (obj instanceof Number) {
            return obj.toString();
        } else if (obj instanceof String) {
            return obj.toString();
        } else if (obj instanceof Boolean) {
            boolean bool = ((Boolean) obj).booleanValue();
            if (bool)  return BOOLEAN_TRUE;
            else return BOOLEAN_FALSE;
        } else {
            return STRING_BLANK;
        }

    }
}
