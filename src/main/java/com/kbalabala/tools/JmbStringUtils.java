package com.kbalabala.tools;

import jodd.util.StringPool;
import jodd.util.StringUtil;

import java.nio.charset.Charset;
import java.util.Map;

import static jodd.util.StringPool.ASTERISK;

/**
 * 积木盒子字符串公共类
 * <p>
 *  <ol>
 *   <li>遮蔽手机号{@link com.kbalabala.tools.JmbStringUtils#maskMobile(String mobile)}</li>
 *   <li>遮蔽身份证号{@link com.kbalabala.tools.JmbStringUtils#maskIdCard(String idCard)}</li>
 *   <li>用'****'替换从start开始到end（不包含）之间的字符串{@link com.kbalabala.tools.JmbStringUtils#mask4Stars(String, int, int)}</li>
 *   <li>用'******'替换从start开始到end（不包含）之间的字符串{@link com.kbalabala.tools.JmbStringUtils#mask4Stars(String, int, int)}</li>
 *   <li>用特定字符串替换从start开始到end（不包含）之间的字符串{@link com.kbalabala.tools.JmbStringUtils#replaceInRange(String origin, int start, int end, String replacement)}</li>
 *   <li>以repeat参数指定数量的＊替换start和end之间的字符{@link com.kbalabala.tools.JmbStringUtils#mask6Stars(String origin, int start, int end)}</li>
 *   <li>去除front和end结合部分的combineChars并且以combineChars连接front和end{@link com.kbalabala.tools.JmbStringUtils#combineWithoutChars(String front, String end, char combineChars)}</li>
 *  </ol>
 * </p>
 * @author kevin
 * @since  2015-4-20
 */
public class JmbStringUtils extends StringUtil {


    public static final String STAR = ASTERISK;
    public static final String STARS_4 = repeat(ASTERISK, 4);
    public static final String STARS_6 = repeat(ASTERISK, 6);

    /**
     * 遮蔽手机号
     * @param mobile
     * @return
     */
    public static String maskMobile(String mobile) {
        return mask4Stars(mobile, 3, 7);
    }

    /**
     * 遮蔽身份证号
     * @param idCard
     * @return
     */
    public static String maskIdCard(String idCard) {

        if(JmbStringUtils.isBlank(idCard)) return idCard;
        switch(idCard.length()){
            case 15:
                return maskXStars(maskXStars(idCard, 3, 6, 3), 12, 13, 1);
            case 18:
                return maskXStars(maskXStars(idCard, 3, 6, 3), 14, 16, 2);
            default:
                return idCard;
        }
    }


    /**
     * 用'****'替换从start开始到end（不包含）之间的字符串
     * @param origin
     * @param start
     * @param end
     * @return
     */
    public static String mask4Stars(String origin, int start, int end) {
        return replaceInRange(origin, start, end, STARS_4);
    }

    /**
     * 用'******'替换从start开始到end（不包含）之间的字符串
     * @param origin
     * @param start
     * @param end
     * @return
     */
    public static String mask6Stars(String origin, int start, int end) {
        return replaceInRange(origin, start, end, STARS_6);
    }

    /**
     * 以repeat参数指定数量的＊替换start和end之间的字符
     * @param origin
     * @param start
     * @param end
     * @param repeat
     * @return
     */
    public static String maskXStars(String origin, int start, int end, int repeat) {
        return replaceInRange(origin, start, end, STAR, repeat);
    }

    /**
     * 去除front和end结合部分的combineChars并且以combineChars连接front和end
     * @param front
     * @param end
     * @param combineChars
     * @return
     */
    public static String combineWithoutChars(String front, String end, char combineChars){
        return stripTrailingChar(front, combineChars) + combineChars + stripLeadingChar(end, combineChars);
    }

    public static String joinPairs(Map<String, Object> params, String pattern, String pairJoiner, String pairsJoiner){

        StringBuilder sb  = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, Object> entry : params.entrySet()){
            if(!first) sb.append(pairsJoiner);
            sb.append(entry.getKey()).append(pairJoiner).append(JmbObjectUtils.toString(entry.getValue(), pattern));
            first = first ? false : first;
        }

        return sb.toString();
    }

    public static byte[] getBytesUtf8(final String string) {
        return getBytes(string, Constants.CS_CHARSET_UTF8);
    }

    /**
     * 字符串－》byte［］
     * @param string
     * @param charset
     * @return
     */
    private static byte[] getBytes(final String string, final Charset charset) {
        if (string == null) {
            return null;
        }
        return string.getBytes(charset);
    }

    /**
     * 用replacement替换从start开始到end（不包含）之间的字符串
     * @param origin
     * @param start ：开始
     * @param end   ：结束位置（不包含）
     * @param replacement
     * @return
     */
    public static String replaceInRange(String origin, int start, int end, String replacement) {
        String lastStr = substring(origin, 0, start) + substring(origin, end, origin.length());
        return insert(lastStr, replacement, start);
    }

    public static String replaceInRange(String origin, int start, int end, String mark, int repeat){
        return replaceInRange(origin,start, end, repeat(StringPool.ASTERISK, repeat));
    }


    public static void main(String[] args){
        System.out.println(StringUtil.fromCamelCase("myNameIsCzheng", '-'));
        System.out.println(StringUtil.capitalize("myNameIsCzheng"));
        System.out.println(StringUtil.crop("myNameIsCzheng"));
        System.out.println(replaceInRange("18910546167", 3, 8, repeat("*", 4)));
        System.out.println(mask4Stars("18910546167", 3, 8));
        System.out.println(mask6Stars("18910546167", 3, 8));
        System.out.println(stripLeadingChar("18910546167", '7'));

    }
}
