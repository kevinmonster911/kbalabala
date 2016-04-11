package com.kbalabala.tools;

/**
 * 类工具类，用于处理关于类的操作<br>
 * <p>
 *  <ol>
 *   <li>获取全类名（含包名）{@link JmbClassUtils#getQualifiedName(Class<?>)}</li>
 *   <li>根据class类实例获取类名（不包含包名）{@link JmbClassUtils#getShortName(Class)}</li>
 *   <li>获取以点分割的类名（不包含包名）{@link JmbClassUtils#getClassNameSplitWithDot(Class)}</li>
 *   <li>获取根指定分隔符分割后的类名（不包含包名）{@link JmbClassUtils#getClassNameWithSpecificSign(Class, String)}</li>
 *  </ol>
 * </p>
 *
 * @author kevin
 * @since  2015/4/17
 *
 */
public class JmbClassUtils {

    /** 类名数组后缀 **/
    public static final String ARRAY_SUFFIX = "[]";

    /** 匹配类名中大写字符与小写字符的位置 **/
    private static final String REGEX_CLASS_NAME_SPLITTER = "(?<=[a-z])(?=[A-Z])";

    /**
     * 获取全类名（含包名）
     * @param clazz
     * @return
     */
    public static String getQualifiedName(Class<?> clazz) {
        if (clazz.isArray()) return getQualifiedNameForArray(clazz);
        else return clazz.getName();
    }

    /**
     * 处理Array类型的类名,如果数组为String[]那么返回String[]
     * @param clazz
     */
    private static String getQualifiedNameForArray(Class<?> clazz) {
        StringBuilder result = new StringBuilder();
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
            result.append(ARRAY_SUFFIX);
        }
        result.insert(0, clazz.getName());
        return result.toString();
    }

    /**
     * 根据class类实例获取类名（不包含包名）
     * @param clazz:类
     * @return
     */
    public static String getShortName(Class<?> clazz) {
        return getShortName(getQualifiedName(clazz));
    }

    public static String getShortName(String className) {
        int lastDotIndex = className.lastIndexOf(Constants.CHAR_SEPARATOR_DOT);
        return className.substring(lastDotIndex + 1, className.length());
    }

    /**
     * 获取以点分割的类名<br>
     * AppLocationDetermine ====> App.Location.Determine
     * @param clazz : 类
     * @return
     */
    public static String getClassNameSplitWithDot(Class<?> clazz) {
        return getClassNameWithSpecificSign(clazz, Constants.CHAR_SEPARATOR_DOT);
    }

    /**
     * 获取以点分割的类名<br>
     * AppLocationDetermine ====> App.Location.Determine
     * @param clazzName : 类名（不包含包）
     * @return
     */
    public static String getClassNameSplitWithDot(String clazzName) {
        return getClassNameWithSpecificSign(clazzName, Constants.CHAR_SEPARATOR_DOT);
    }

    /**
     * 获取根指定分隔符进行拼装后的类名<br>
     * 类名:AppLocationDetermine 分隔符:- ====> App.Location.Determine
     * @param clazz
     * @param separator
     * @return
     */
    public static String getClassNameWithSpecificSign(Class<?> clazz, String separator) {
        String clazzName = getShortName(clazz);
        return getClassNameWithSpecificSign(clazzName, separator);
    }

    /**
     * 获取根据指定分隔符进行拼装后的类名<br>
     * 类名:AppLocationDetermine 分隔符:- ====> App.Location.Determine
     * @param clazzName
     * @param separator
     * @return
     */
    public static String getClassNameWithSpecificSign(String clazzName, String separator) {
        return clazzName.replaceAll(REGEX_CLASS_NAME_SPLITTER, separator);
    }
}